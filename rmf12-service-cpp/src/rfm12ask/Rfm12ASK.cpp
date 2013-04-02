/*
 * Rfm12ASK.cpp
 *
 *  Created on: 31.03.2013
 *      Author: florian
 */

#include "Rfm12ASK.h"
#include <wiringPiSPI.h>
#include <wiringPi.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <string.h>
#include <errno.h>
using namespace std;

#define RF_RECEIVER_ON  0x82C9
#define RF_XMITTER_ON   0x8238
#define RF_IDLE_MODE    0x8209
#define RF_mySleep_MODE   0x8201
#define RF_WAKEUP_MODE  0x8203
#define RF_LOW_POWER_MODE  0x9807
#define RF_HIGH_POWER_MODE  0x9800


Rfm12ASK::Rfm12ASK() {
	// TODO Auto-generated constructor stub

}

Rfm12ASK::~Rfm12ASK() {
	// TODO Auto-generated destructor stub
}


void Rfm12ASK::rf12_xfer(unsigned int data) {
	unsigned char myChar[2];
	memcpy(myChar, (char*)&data, 2);

	unsigned char result[2];
	result[0]=myChar[1];
	result[1]=myChar[0];

	if (wiringPiSPIDataRW(0, result, 2) < 0) {
		fprintf(stderr, "SPI send failed: %s\n", strerror(errno));

	}
}

void Rfm12ASK::rf12DisableTransmitter(){
	rf12_xfer(RF_IDLE_MODE);
}

void Rfm12ASK::rf12TransmitHigh() {
	rf12_xfer(RF_HIGH_POWER_MODE);
}

void Rfm12ASK::rf12EnableTransmitter(){
	rf12_xfer(RF_XMITTER_ON);
	delay(15);
}

void Rfm12ASK::rf12TransmitLow(){
	rf12_xfer(RF_LOW_POWER_MODE);
}

int Rfm12ASK::rf12SetupTx() {
	piHiPri(99);

	if (wiringPiSPISetup(0, 1000000) < 0) {
		fprintf(stderr, "SPI Setup failed: %s\n", strerror(errno));
	//	return -1;
	}

	rf12_xfer(0xFF00); //reset RFM12
	delay(50);
	//init device
	rf12_xfer(0x8017); // disable FIFO, 433MHz, 12.0pF
	rf12_xfer(0x8208); // synth off, PLL off, enable xtal, enable clk pin, disable Batt
	rf12_xfer(0xA620); //   (0xA620 433.92MHz)
	rf12_xfer(0xC647); // c647 4.8Kbps (38.4: 8, 19.2: 11, 9.6: 23, 4.8: 47)
	rf12_xfer(0x9489); // VDI,FAST,BW200kHz,-6dBm,DRSSI -97dbm
	rf12_xfer(0xC220); // datafiltercommand ; ** not documented command **
	rf12_xfer(0xCA00); // FiFo and resetmode command ; FIFO fill disabeld
	rf12_xfer(0xC4C3); // enable AFC ;enable frequency offset
	rf12_xfer(0xCC67); //
	rf12_xfer(0xC000); // clock output 1.00MHz, can be used to see if SPI works
	rf12_xfer(0xE000); // disable wakeuptimer
	rf12_xfer(0xC800); // disable low duty cycle
	delay(50);
	return 0;
}

int Rfm12ASK::setupRx() {

	piHiPri(99);

	if (wiringPiSPISetup(0, 1000000) < 0) {
		fprintf(stderr, "SPI Setup failed: %s\n", strerror(errno));
	//	return -1;
	}

	rf12_xfer(0x8027); // disable FIFO, 868MHz(!), 12.0pF
	rf12_xfer(0x82C9); // synth off, PLL off, enable xtal, disable clk pin, disable Batt
	rf12_xfer(0xA620); //   (0xA620 433.92MHz)
	rf12_xfer(0xC647); // c647 4.8Kbps (38.4: 8, 19.2: 11, 9.6: 23, 4.8: 47)
	rf12_xfer(0x9489); // VDI,FAST,BW200kHz,-6dBm,DRSSI -97dbm
	rf12_xfer(0xC220); // datafiltercommand ; ** not documented command **
	rf12_xfer(0xCA00); // FiFo and resetmode command ; FIFO fill disabeld
	rf12_xfer(0xC4C3); // enable AFC ;enable frequency offset
	rf12_xfer(0xCC67); //
	rf12_xfer(0xC000); // clock output 1.00MHz, can be used to see if SPI works
	rf12_xfer(0xE000); // disable wakeuptimer
	rf12_xfer(0xC800); // disable low duty cycle

	return 0;
}
