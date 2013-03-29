/*
 * DataSocketHandling.cpp
 *
 *  Created on: 28.11.2012
 *      Author: florian
 */

#include "DataSocketHandling.h"
#include "../tmp1812/Tmp1812SPI.h"
#include <sys/socket.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <map>

using namespace std;

DataSocketHandling::DataSocketHandling() {
	// TODO Auto-generated constructor stub
}

DataSocketHandling::~DataSocketHandling() {
	// TODO Auto-generated destructor stub
}

void DataSocketHandling::handleDataRequests(int &workingControlSocket,
	map<int, int> &stripePortMapping) {

	Tmp1812SPI spiDataSend;
	spiDataSend.setup();

	map<int, int>::iterator p;

	do {
		for (p = stripePortMapping.begin(); p != stripePortMapping.end(); p++) {
			//later this may be needed to send data to the right port!
			int portNumber = p->first;
			int pixelAmount = p->second;

			unsigned char* data = (unsigned char *) malloc(3*
					pixelAmount * sizeof(unsigned char));

			if (readData(workingControlSocket, data, 3*pixelAmount) <= 0) {
				return;
			}

			//printout routine
			/*
			unsigned char* values = data;
			int j;

			printf("Client sent: ");
			for (j = 0; j < 3*pixelAmount; j++) {
				printf("%x ", *values++);
				if((j+1)%3==0){
					printf("|");
				}
			}
			printf("\n");
			fflush(stdout);
*/
			spiDataSend.sendData(portNumber,data,3*pixelAmount);

			free(data);
		}
	} while (1);
}

ssize_t DataSocketHandling::readData(int sockd, void *vptr, size_t maxlen) {
	ssize_t rc;
	rc = recv(sockd, vptr, maxlen, 0);
	return rc;
}

