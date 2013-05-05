/*
 * DataSocketHandling.cpp
 *
 *  Created on: 28.11.2012
 *      Author: florian
 */

#include "DataSocketHandling.h"
#include "../tmp1812/Tmp1812SPI.h"
#include "../directspi/DirectSPI.h"
#include "StripePortMapping.h"
#include <sys/socket.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <map>

#define TM1812 "tm1812"
#define DIRECT_SPI "directSpi"

using namespace std;

DataSocketHandling::DataSocketHandling() {
	// TODO Auto-generated constructor stub
}

DataSocketHandling::~DataSocketHandling() {
}

void DataSocketHandling::handleDataRequests(int &workingControlSocket, map<int, StripePortMapping> &stripePortMapping) {

	map<int, StripePortMapping>::iterator p;

	do {
		for (p = stripePortMapping.begin(); p != stripePortMapping.end(); p++) {
			//later this may be needed to send data to the right port!
			int portNumber = p->first;
			StripePortMapping mapping = p->second;
			int pixelAmount = mapping.pixelAmount;
			string protocoll = mapping.protocollType;

			unsigned char* data = (unsigned char *) malloc(3 * pixelAmount * sizeof(unsigned char));

			if (readData(workingControlSocket, data, 3 * pixelAmount) <= 0) {
				return;
			}

			if (strcmp(protocoll.c_str(), TM1812) == 0) {
				Tmp1812SPI spiDataSend;
				spiDataSend.setup();
				spiDataSend.sendData(portNumber, data, 3 * pixelAmount);
			}

			if (strcmp(protocoll.c_str(), DIRECT_SPI) == 0) {
				DirectSPI spiDataSend;
				spiDataSend.setup();
				spiDataSend.sendData(portNumber, data, 3 * pixelAmount);
			}

			free(data);
		}
	} while (1);
}

ssize_t DataSocketHandling::readData(int sockd, void *vptr, size_t maxlen) {
	ssize_t rc;
	rc = recv(sockd, vptr, maxlen, 0);
	return rc;
}

