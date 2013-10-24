#include "Crc.h"
#include "Util.h"
#include "MaxRF22.h"
#include "MaxRFProto.h"
#include "Pn9.h"
#include <iostream>
#include <wiringPi.h>
#include "Logger.h"

//static_assert(PN9_LEN >= RF22_MAX_MESSAGE_LEN, "Not enough pn9 bytes defined");

MaxRF22 rf(0x00);

void printStatus() {
	for (unsigned int i = 0; i < lengthof(devices); ++i) {
		Device *d = &devices[i];
		if (!d->address) {
			std::cout << "stopped status at " << i << "\n\r";
			break;
		}
		if (d->type != DeviceType::RADIATOR && d->type != DeviceType::WALL)
			continue;

		if (d->name) {
			std::cout << d->name;
		} else {
			/* Only print two bytes on the lcd to save space */
			std::cout << d->address;
		}
		std::cout << " " << "ActualTemp" << d->actual_temp << "/" << "SetTemp" << d->set_temp;
		if (d->type == DeviceType::RADIATOR)
			std::cout << " " "ValvePos" << d->data.radiator.valve_pos;
		std::cout << "\n";
	}
	std::cout << "\n";
}

void dump_buffer(uint8_t *buf, uint8_t len) {
	/* Dump the raw received data */
	std::cout << "\r\ndump: \r\n";
	for (int i = 0; i < len; i++) {
		fprintf(stdout, " %x", buf[i]);
	}
	fflush(stdout);
	std::cout << "\r\n";
}

void setup() {

	if (rf.init())
		printf("RF init OK\n");
	else
		printf("RF init failed\n");

	std::cout << "Initialized" << "\r\n";
	//printStatus();
}

void loop() {
	uint8_t buf[RF22_MAX_MESSAGE_LEN];
	uint8_t len = 20;

	if (rf.recv(buf, &len)) {
		/* Enable reception right away again, so we won't miss the next
		 * message while processing this one. */
		rf.setModeRx();

		std::cout << "Received " << len << " bytes" << "\r\n";

		dump_buffer(buf, len);

		if (len < 3) {
			std::cout << "Invalid packet length (" << len << ")" << "\r\n";
			return;
		}

		/* Dewhiten data */
		if (xor_pn9(buf, len) < 0) {
			std::cout << "Invalid packet length (" << len << ")" << "\r\n";
			return;
		}

		std::cout << "Dewhitened:" << "\r\n";
		dump_buffer(buf, len);

		len = buf[0] + 3; // 1 length-Byte + 2 CRC

		/* Calculate CRC (but don't include the CRC itself) */
		uint16_t crc = calc_crc(buf, len - 2);
		if (buf[len - 1] != (crc & 0xff) || buf[len - 2] != (crc >> 8)) {
			std::cout << "CRC error" << "\r\n";
			//return;
		}

		/* Parse the message (without length byte and CRC) */
		MaxRFMessage *rfm = MaxRFMessage::parse(buf + 1, len - 3);

		if (rfm == NULL) {
			std::cout << "Packet is invalid" << "\r\n";
		} else {
			std::cout << "found something\r\n";
			std::cout << rfm->type_to_str(rfm->type) << "\r\n";

			std::cout << rfm->addr_from << "\r\n";
			std::cout << rfm->addr_to << "\r\n";
			rfm->updateState();
		}
		delete rfm;

		printStatus();

		std::cout << "\r\n";
	}
}

int main(int argc, const char* argv[]) {

	setup();

	if (argc == 2) {
		char *endptr;
		int samples = strtol(argv[1], &endptr, 0);
		if (*endptr) {
			fprintf(stderr, "Invalid sample number.\n");
			exit (EXIT_FAILURE);
		}
		printf("logging %d samples\n", samples);
		fflush(stdout);

		const uint8_t sync[] = {
			  0x12,
			  0xAB,
			  0xCB,
			  0xA8,
			};
			rf.setSyncWords(sync,0x4);


		rf.setModeRx();
		Logger logging(&rf,samples);
		logging.loop();
		std::cout << "finished";
	} else {

		const uint8_t sync[] = {
		  0x55,
		  0x55,
		  0x55,
		  0x55,
		};
		uint8_t length=50;
		rf.spiWrite(RF22_REG_3E_PACKET_LENGTH, length);
		rf.setSyncWords(sync,0x4);


		uint8_t data[50];
		for(int i=0;i<50;i++){
			data[i]=0x55;
		}
		rf.send(data,length);
		rf.send(data,length);
		rf.send(data,length);
		rf.send(data,length);
		rf.send(data,length);
		rf.send(data,length);
		rf.send(data,length);
		rf.send(data,length);


		rf.waitPacketSent();
	//	delay(5);
		rf.spiWrite(RF22_REG_3E_PACKET_LENGTH, 20);
		const uint8_t sync_words[] = {
		  0xc6,
		  0x26,
		  0xc6,
		  0x26,
		};
		rf.setSyncWords(sync_words,4);
		uint8_t sendData[14] = { 0xf4, 0xc4, 0x18, 0xda, 0xef, 0xa, 0xf1, 0x22, 0x7f, 0xb1, 0xd3, 0x12, 0xde, 0xde };
		rf.send(sendData, 14);

		delay(100);
		rf.init();
		delay(100);
		while (true) {
			loop();
			delay(1);
			fflush(stdout);
		}
//	uint8_t sendData[11] = { 0xf4, 0xc4, 0x18, 0xda, 0xef, 0xa, 0xf1, 0x22, 0x7f, 0xb1, 0xd3, 0x12, 0xde, 0xde, 0xa, 0x30, 0x60,
//			0x29, 0x5c, 0xb0 };
	}
}

