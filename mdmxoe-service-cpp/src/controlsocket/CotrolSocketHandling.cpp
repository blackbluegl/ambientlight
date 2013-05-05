/*
 * cotrolsockethandling.cpp
 *
 *  Created on: 28.11.2012
 *      Author: florian
 */

#include "CotrolSocketHandling.h"

#include <sys/socket.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <map>

using namespace std;

CotrolSocketHandling::CotrolSocketHandling() {
}

CotrolSocketHandling::~CotrolSocketHandling() {
}


#define MAX_PAYLOAD (6000)


/*the client connects to this socket. initializes the stripe ports
  and the stripe led amount and quits to send data afterwards*/
map<int,StripePortMapping> CotrolSocketHandling::handleControlRequests(int workingControlSocket) {

	//TODO stripeport is indirectly calulated by sequence number. this should be changed and given by the config of a stripe

	map<int,StripePortMapping> stripePortMapping;
	int stripePortForNextRequest = -1;
	string protocollTypeForNextRequest;
	string RESPONSE_OK = "OK\n";

	while (1) {
		/* read a line*/
		string buffer;

		/* end if client closed connection or a failure did occour */
		buffer=readLine(workingControlSocket);
		if (buffer.empty()) {
			return stripePortMapping;
		}

		//set the stripe port mapping array index - get position and keep for next value
		int currentStripePortNumber = getCommandIntValue(buffer, "stripe_port");
		if (currentStripePortNumber > -1) {
			stripePortForNextRequest = currentStripePortNumber;
			writeLine(workingControlSocket, RESPONSE_OK);
		}

		//set stripeType
		string currentStripeType = getCommandStringValue(buffer, "protocoll_type");
		if (currentStripeType.empty() == false) {
			protocollTypeForNextRequest = currentStripeType;
			writeLine(workingControlSocket, RESPONSE_OK);
		}

		//set the stripe port mapping array value
		int currentPixelSize = getCommandIntValue(buffer, "pixel_size");
		if (currentPixelSize > -1) {
			StripePortMapping mapping;
			mapping.pixelAmount=currentPixelSize;
			mapping.protocollType= protocollTypeForNextRequest;
			stripePortMapping[stripePortForNextRequest]=mapping;

			writeLine(workingControlSocket, RESPONSE_OK);
		}
	}
	return stripePortMapping;
}


string CotrolSocketHandling::readLine(int &socked) {
	ssize_t rc;
	char c;
	string buffer = "";

	do{
		if ((rc = read(socked, &c, +1)) == 1) {
			buffer.append(&c);
			if (c == '\n') {
				break;
			}
		}
	}
	while(rc>0);
	return buffer;
}


void CotrolSocketHandling::writeLine(int socked, string &message) {
	send(socked,message.c_str(),message.size(),0);
}


int CotrolSocketHandling::getCommandIntValue(string &inputString, string commandName) {

	if(inputString.find(commandName) == string::npos){
		return -1;
	}

	size_t valueStartPosition = inputString.rfind("=");

	if (valueStartPosition != string::npos) {

		string stringResult = inputString.substr (valueStartPosition+1,inputString.size()-1);

		int result = atoi(stringResult.c_str());
		return result;
	}
	return -1;
}

string CotrolSocketHandling::getCommandStringValue(string &inputString, string commandName) {

	if (inputString.find(commandName) == string::npos) {
		return string();
	}

	size_t valueStartPosition = inputString.rfind("=");

	if (valueStartPosition != string::npos) {

		return inputString.substr(valueStartPosition + 1, inputString.size() - 1);
	}
	return NULL;
}




