/*
 * MaxDispatcherModule.cpp
 *
 *  Created on: 27.10.2013
 *      Author: florian
 */

#include "MaxDispatcherModule.h"
#include "../DispatcherModule.h"
#include "../../rfm22/RF22.h"
#include "Crc.h"
#include "Pn9.h"
//#include "../../queue/OutMessage.h"
#include <vector>
#include "../../queue/InMessage.h"
#include "MaxRFProto.h"
#include <iostream>
#include <sstream>
#include "../../queue/Enums.h"

MaxDispatcherModule::~MaxDispatcherModule() {

}

bool MaxDispatcherModule::init(RF22 *rf22) {
	rf22->setModemRegisters(&maxModemConfig);
	rf22->setFrequency(868.3, 0.035);

	rf22->spiWrite(RF22_REG_2A_AFC_LIMITER, 0x1C);
	rf22->spiWrite(RF22_REG_30_DATA_ACCESS_CONTROL, RF22_MSBFRST | RF22_ENPACRX | RF22_ENPACTX);
	/* No packet headers, 4 sync words, fixed packet length */
	rf22->spiWrite(RF22_REG_32_HEADER_CONTROL1, RF22_BCEN_NONE | RF22_HDCH_NONE);
	rf22->spiWrite(RF22_REG_33_HEADER_CONTROL2, RF22_HDLEN_0 | RF22_FIXPKLEN | RF22_SYNCLEN_4);
	rf22->setSyncWords(maxSyncWords, sizeof(maxSyncWords));
	/* Detect preamble after 4 nibbles */
	rf22->spiWrite(RF22_REG_35_PREAMBLE_DETECTION_CONTROL1, (0x4 << 3));
	/* Send 8 bytes of preamble - but that depends on the message type and will be handled dynamically*/
	rf22->setPreambleLength(255); // in nibbles
	rf22->spiWrite(RF22_REG_3E_PACKET_LENGTH, incommingMessageLength);
	return true;
}

void MaxDispatcherModule::sendMessage(RF22 *rf22, OutMessage message) {

	//send short preemble if last Send was not longer than 4  seconds in the past
	time_t now;
	now = time(NULL);
	bool sendLong = false;
	if (lastSendTimeStamp + 3 < now) {
		sendLong = true;
	}

	if (message.payLoad.size() > 2 && message.payLoad.at(2) == 0x02) {
		cout << "MaxDispatcher - sendMessage(): sending an \"ACK\" and therefore a short preamble\n";
		sendLong = false;
	}

	if (message.payLoad.size() > 2 && message.payLoad.at(2) == 0x01) {
		cout << "MaxDispatcher - sendMessage(): sending an \"PONG\" and therefore a short preamble\n";
		sendLong = false;
	}

	if (sendLong == true) {
		cout << "MaxDispatcher - sendMessage(): sending long preamble\n";
	} else {
		cout << "MaxDispatcher - sendMessage(): sending short preamble\n";
	}

	sendLongPreamble(rf22, sendLong);

	//create message
	unsigned int length = message.payLoad.size() + 3;
	uint8_t sendData[length];
	rf22->spiWrite(RF22_REG_3E_PACKET_LENGTH, length);

	//set payload length to first byte
	sendData[0] = length - 3;

	//set payload
	for (unsigned int i = 0; i < message.payLoad.size(); i++) {
		sendData[i + 1] = message.payLoad.at(i);
	}

	//create crc in the last 2 bytes
	uint16_t scrc = calc_crc(sendData, length - 2);
	sendData[length - 2] = scrc >> 8;
	sendData[length - 1] = scrc & 0xff;

	//dewhitening
	xor_pn9(sendData, length);

	//send message
	rf22->send(sendData, length);
	rf22->waitPacketSent();

	lastSendTimeStamp = time(NULL);
}

void MaxDispatcherModule::receiveMessage(RF22 *rf22) {
	uint8_t data[incommingMessageLength];
	uint8_t incommingLength = incommingMessageLength;

	if (rf22->recv(data, &incommingLength) == false) {
		return;
	}

	// Enable reception right away again, so we won't miss the next message while processing this one.
	rf22->setModeRx();

	if (incommingLength < 3) {
		return;
	}

	// Dewhiten data
	if (xor_pn9(data, incommingLength) < 0) {
		return;
	}

	uint8_t len = data[0] + 3; // 1 length-Byte + 2 CRC

	// Calculate CRC (but don't include the CRC itself)
	uint16_t crc = calc_crc(data, len - 2);
	if (data[len - 1] != (crc & 0xff) || data[len - 2] != (crc >> 8)) {
		return;
	}

	// Parse the message (without length byte and CRC)
	MaxRFMessage *rfMessage = MaxRFMessage::parse(data + 1, len - 3);

	if (rfMessage == NULL) {
		std::cout << "Packet is invalid" << "\r\n";
		return;
	} else {
		std::cout << "MaxDispatcherModule receiveMessage(): got MAX! Message: " << rfMessage->type_to_str(rfMessage->type)
				<< " from " << rfMessage->addr_from << " to " << rfMessage->addr_to << "\r\n";
	}

//	if (rfMessage->type == MessageType::SHUTTER_CONTACT_STATE) {
//		//we have to create a response very fast for the shuttercontact
//		OutMessage response;
//		response.payLoad.push_back(rfMessage->seqnum);
//		response.payLoad.push_back(0x0); //flags maybe wrong. have to sniff again
//		response.payLoad.push_back(0x2); //ack
//		response.payLoad.push_back(data[7]); //swap from and to
//		response.payLoad.push_back(data[8]);
//		response.payLoad.push_back(data[9]);
//		response.payLoad.push_back(data[4]);
//		response.payLoad.push_back(data[5]);
//		response.payLoad.push_back(data[6]);
//		response.payLoad.push_back(data[10]); //set the same group id
//		response.payLoad.push_back(0x0); //simple ack
//		response.dispatchTo = Enums::DispatcherType::MAX;
//		dispatcher->sendADirectResponse(response);
//		cout << "MaxDispatcherModule - receiveMessage(): send as soon as possible an ack for: " << rfMessage->addr_from << "\n";
//	}

	InMessage message;
	message.dispatchTo = Enums::MAX;

	std::stringstream fromStream;
	fromStream << rfMessage->addr_from;

	message.correlation = "MAX_" + fromStream.str();
	for (uint8_t i = 1; i < len - 2; i++) {
		message.payload.push_back(data[i]);
	}

	delete rfMessage;
	dispatcher->dispatchInMessage(message);
}

void MaxDispatcherModule::switchToTX(RF22 *rf22) {

}

void MaxDispatcherModule::switchToRx(RF22 *rf22) {
	rf22->spiWrite(RF22_REG_3E_PACKET_LENGTH, incommingMessageLength);
	rf22->setModeRx();
}

bool MaxDispatcherModule::sendLongPreamble(RF22 *rf22, bool longPreamble) {
	// We do not change the complete modem settings. we just adapt the data and preamble to send a 0101 sequence.
	const uint8_t syncLong[] = { 0x55, 0x55, 0x55, 0x55, };
	rf22->setSyncWords(syncLong, 0x4);

	uint8_t fakePreambleLength = 50;
	rf22->spiWrite(RF22_REG_3E_PACKET_LENGTH, fakePreambleLength);
	uint8_t data[fakePreambleLength];
	for (int i = 0; i < fakePreambleLength; i++) {
		data[i] = 0x55;
	}
	if (longPreamble == true) {
		for (int i = 0; i < 10; i++) {
			rf22->send(data, fakePreambleLength);
		}
	} else {
		rf22->send(data, fakePreambleLength);
	}

	//prepare modem and reset sync word
	rf22->waitPacketSent();
	rf22->setSyncWords(maxSyncWords, 4);
	return true;
}
