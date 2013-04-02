/*
 * SwitchHandler.cpp
 *
 *  Created on: 31.03.2013
 *      Author: florian
 */

#include "SwitchHandler.h"
#include <string.h>
#include <stdlib.h>     /* atoi */
#include "../rfm12ask/Rfm12ASK.h"


using namespace std;


SwitchHandler::SwitchHandler() {
	// TODO Auto-generated constructor stub
}

SwitchHandler::~SwitchHandler() {
	// TODO Auto-generated destructor stub
}

void SwitchHandler::handleSwitch(std::string commandValues) {
	std::string::size_type prev_pos = 0, pos = 0;

	pos = commandValues.find('|', pos);
	std::string systemString(commandValues.substr(prev_pos, pos - prev_pos));
	prev_pos = ++pos;

	pos = commandValues.find('|', pos);
	std::string substringHouseCode(
			commandValues.substr(prev_pos, pos - prev_pos));
	int houseCode = atoi(substringHouseCode.c_str());
	prev_pos = ++pos;

	pos = commandValues.find('|', pos);
	std::string substringSwitchNumber(
			commandValues.substr(prev_pos, pos - prev_pos));
	int switchNumber = atoi(substringSwitchNumber.c_str());
	prev_pos = ++pos;

	pos = commandValues.find('|', pos);
	std::string substringPowerState(
			commandValues.substr(prev_pos, pos - prev_pos));
	int powerState = atoi(substringPowerState.c_str());

	if (systemString.compare("ELRO") == 0) {
		handleElroSwitch(houseCode, switchNumber, powerState);
	}

	return;
}

void SwitchHandler::handleElroSwitch(int houseCode, int switchNumber, int powerState) {
	Rfm12ASK rfm12;
	rfm12.rf12SetupTx();
	rfm12.rf12EnableTransmitter();

	for (int i = 0; i < 5; i++) {
		//send data here
		sendHouseCode(houseCode);
		sendSwitchCode(switchNumber);
		sendPowerCode(powerState);
		sendSync();
	}

	rfm12.rf12DisableTransmitter();

}

void SwitchHandler::sendPowerCode(int code) {
	if (code == 1) {
		send0();
		sendF();
	} else {
		sendF();
		send0();
	}
}

void SwitchHandler::sendHouseCode(int housecode) {
	char houseCodeBitMask = housecode;
	//the housecode is only 5 bits long. so we ignore the first 3.

	for (int i = 3; i < 8; i++) {
		//test the current bit if it is set or not
		if (houseCodeBitMask & (1 << i)) {
			sendF();
		} else {
			send0();
		}
	}
}

void SwitchHandler::sendSwitchCode(int number) {
	for (int i = 1; i <= 5; i++) {
		if (i == number) {
			send0();
		} else {
			sendF();
		}
	}
}

void SwitchHandler::send0() {
	Rfm12ASK rfm12;
	rfm12.rf12TransmitHigh();
	microSleep(400);
	rfm12.rf12TransmitLow();
	microSleep(1000);
	rfm12.rf12TransmitHigh();
	microSleep(400);
	rfm12.rf12TransmitLow();
	microSleep(1000);
}

void SwitchHandler::sendF() {
	Rfm12ASK rfm12;
	rfm12.rf12TransmitHigh();
	microSleep(400);
	rfm12.rf12TransmitLow();
	microSleep(1000);
	rfm12.rf12TransmitHigh();
	microSleep(1000);
	rfm12.rf12TransmitLow();
	microSleep(400);
}

void SwitchHandler::sendSync() {
	Rfm12ASK rfm12;
	rfm12.rf12TransmitHigh();
	microSleep(400);
	rfm12.rf12TransmitLow();
	microSleep(10000);
}

void SwitchHandler::microSleep(int microsec) {
	struct timespec req = { 0 };
	req.tv_sec = 0;
	req.tv_nsec = microsec * 1000L;
	nanosleep(&req, (struct timespec *) NULL);
}
