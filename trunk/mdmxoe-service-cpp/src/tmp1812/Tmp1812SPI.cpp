/*
 * Tmp1812SPI.cpp
 *
 *  Created on: 30.11.2012
 *      Author: florian
 */

#include "Tmp1812SPI.h"
#include <stdint.h>
#include <errno.h>
#include <string.h>
#include <iostream>
#include <string>
#include <wiringPiSPI.h>
#include <wiringPi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

using namespace std;

Tmp1812SPI::Tmp1812SPI() {
	// TODO Auto-generated constructor stub

}

Tmp1812SPI::~Tmp1812SPI() {
	// TODO Auto-generated destructor stub
}

int Tmp1812SPI::setup() {
	piHiPri(99);

	if (wiringPiSPISetup (0, 2000000) < 0){
	  fprintf (stderr, "SPI Setup failed: %s\n", strerror (errno));
	  return -1;
	}
	return 0;
}


int Tmp1812SPI::sendData(int channel, unsigned char *buffer, int sizeOfBuffer) {
	int wordSize = 4;
	int spiSendBufferSize = sizeOfBuffer * wordSize;

	unsigned char spiDataBuffer[spiSendBufferSize];
	unsigned char *singleCharFromBuffer = buffer;

	for (int i = 0; i < sizeOfBuffer; i++) {
		unsigned char byte = *singleCharFromBuffer++;
		string result = convertByteToTm1812Word(byte);

		unsigned char spiWord1 = result.at(0);
		unsigned char spiWord2 = result.at(1);
		unsigned char spiWord3 = result.at(2);
		unsigned char spiWord4 = result.at(3);

		spiDataBuffer[i * 4 + 0] = spiWord1;
		spiDataBuffer[i * 4 + 1] = spiWord2;
		spiDataBuffer[i * 4 + 2] = spiWord3;
		spiDataBuffer[i * 4 + 3] = spiWord4;
	}

	const int wordToSendAtOnce = spiSendBufferSize;

	unsigned char spiSend[wordToSendAtOnce];

	for (int y = 0; y < spiSendBufferSize; y = y + wordToSendAtOnce) {
		for (int i = 0; i < wordToSendAtOnce; i++) {
			spiSend[i] = spiDataBuffer[y + i];
		}
		if (wiringPiSPIDataRW(channel, spiSend, wordToSendAtOnce) < 0) {
			fprintf(stderr, "SPI send failed: %s\n", strerror(errno));
		}
	}
	return 0;
}

/**
 * if we use 4 mhz as timing and code single bits as 110 or 100, we can generate a protocol on the spi
 * according the tm1812 protocol and timing. so we can use the spi bus and do not have to take care of bitbanging.
 */
string Tmp1812SPI::convertByteToTm1812Word(unsigned char byte) {
	unsigned char spiWord1 = { 0xE }; //was 0x7
		unsigned char spiWord0 = { 0x4 }; //was 0x2

		const int wordSize = 4;
		char tm1812Word[wordSize] = { "" };

		for (int y = 0; y < 8; y++) {
			//shift all bits by three to the left14
			for (int i = 1; i < wordSize; i++) {
				unsigned char charAtIminus1 = tm1812Word[i - 1];
				unsigned char charAtI = tm1812Word[i];
				unsigned char overfloatAtI = charAtI >> 4;

				charAtIminus1 = charAtIminus1 << 4;
				charAtIminus1 = charAtIminus1 | overfloatAtI;
				tm1812Word[i - 1] = charAtIminus1;
			}
			//at the end shift bits of last byte by 3 to the left and free last 3 bits
			unsigned char lastChar = tm1812Word[wordSize - 1];
			lastChar = lastChar << 4;

			//after shifting paste in bits according testResult of input bits value
			unsigned char testBit = { 128 };
			if (byte & (testBit >> y)) {
				lastChar = lastChar | spiWord1;
			} else {
				lastChar = lastChar | spiWord0;
			}

			//now set last byte
			tm1812Word[wordSize - 1] = lastChar;
		}

		string result(tm1812Word);
		return result;
}
