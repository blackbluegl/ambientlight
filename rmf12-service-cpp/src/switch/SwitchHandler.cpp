/*
 * SwitchHandler.cpp
 *
 *  Created on: 31.03.2013
 *      Author: florian
 */

#include "SwitchHandler.h"
#include <string.h>
#include <stdlib.h>
#include "../rfm12ask/Rfm12ASK.h"

using namespace std;

int zeroWait=320;//was 400
int oneWait=900;//was oneWait

SwitchHandler::SwitchHandler() {
}

SwitchHandler::~SwitchHandler() {
}

void SwitchHandler::handleSwitch(std::string commandValues) {
	std::string::size_type prevPos = 0, pos = 0;

	pos = commandValues.find('|', pos);
	string systemString(commandValues.substr(prevPos, pos - prevPos));
	prevPos = ++pos;

	pos = commandValues.find('|', pos);
	string substringHouseCode(commandValues.substr(prevPos, pos - prevPos));
	int houseCode = atoi(substringHouseCode.c_str());
	prevPos = ++pos;

	pos = commandValues.find('|', pos);
	string substringSwitchNumber(commandValues.substr(prevPos, pos - prevPos));
	int switchNumber = atoi(substringSwitchNumber.c_str());
	prevPos = ++pos;

	pos = commandValues.find('|', pos);
	string substringPowerState(commandValues.substr(prevPos, pos - prevPos));
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

	for (int i = 0; i < 6; i++) {
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
	microSleep(zeroWait);
	rfm12.rf12TransmitLow();
	microSleep(oneWait);
	rfm12.rf12TransmitHigh();
	microSleep(zeroWait);
	rfm12.rf12TransmitLow();
	microSleep(oneWait);
}

void SwitchHandler::sendF() {
	Rfm12ASK rfm12;
	rfm12.rf12TransmitHigh();
	microSleep(zeroWait);
	rfm12.rf12TransmitLow();
	microSleep(oneWait);
	rfm12.rf12TransmitHigh();
	microSleep(oneWait);
	rfm12.rf12TransmitLow();
	microSleep(zeroWait);
}

void SwitchHandler::sendSync() {
	Rfm12ASK rfm12;
	rfm12.rf12TransmitHigh();
	microSleep(zeroWait);
	rfm12.rf12TransmitLow();
	microSleep(10000);
}

void SwitchHandler::microSleep(int microsec) {
	struct timespec req = { 0 };
	req.tv_sec = 0;
	req.tv_nsec = microsec * 1000L;
	nanosleep(&req, (struct timespec *) NULL);
}
