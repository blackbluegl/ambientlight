/*
 * DirectSPI.h
 *
 *  Created on: 30.11.2012
 *      Author: florian
 */

#ifndef DIRECTSPI_H_
#include <wiringPi.h>
#include <string>

using namespace std;

#define DIRECTSPI_H_

class DirectSPI {
public:
	DirectSPI();
	virtual ~DirectSPI();
	int setup();
	int sendData(int channel, unsigned char *buffer, int sizeOfBuffer);
};

#endif /* DIRECTSPI_H_ */
