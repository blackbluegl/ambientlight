/*
 * Tmp1812SPI.h
 *
 *  Created on: 30.11.2012
 *      Author: florian
 */

#ifndef TMP1812SPI_H_
#include <wiringPi.h>
#include <string>

using namespace std;

#define TMP1812SPI_H_

class Tmp1812SPI {
public:
	Tmp1812SPI();
	virtual ~Tmp1812SPI();
	int setup(int port);
	int sendData(int channel, unsigned char *buffer, int sizeOfBuffer);
private:
	string convertByteToTm1812Word(unsigned char byte);
};

#endif /* TMP1812SPI_H_ */
