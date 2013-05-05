/*
 * DirectSPI.cpp
 *
 *  Created on: 30.11.2012
 *      Author: florian
 */

#include "DirectSPI.h"
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

DirectSPI::DirectSPI() {
}

DirectSPI::~DirectSPI() {
}


int DirectSPI::setup() {
	piHiPri(99);

	if (wiringPiSPISetup (0, 1000000) < 0){
	  fprintf (stderr, "SPI Setup failed: %s\n", strerror (errno));
	  return -1;
	}
	return 0;
}


int DirectSPI::sendData(int channel, unsigned char *buffer, int sizeOfBuffer) {

		if (wiringPiSPIDataRW(channel, buffer, sizeOfBuffer) < 0) {
			fprintf(stderr, "SPI send failed: %s\n", strerror(errno));
		}
	return 0;
}
