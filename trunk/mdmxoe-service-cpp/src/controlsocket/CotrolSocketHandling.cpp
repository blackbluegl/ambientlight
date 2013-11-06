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
map<int, StripePortMapping> CotrolSocketHandling::handleControlRequests(int workingControlSocket) {

	//TODO stripeport is indirectly calulated by sequence number. this should be changed and given by the config of a stripe

	map<int, StripePortMapping> stripePortMapping;

	string RESPONSE_OK = "OK\n";

	while (1) {
		/* read a line*/
		string buffer;

		/* end if client closed connection or a failure did occour */
		buffer = readLine(workingControlSocket);
		if (buffer.empty()) {
			return stripePortMapping;
		}

		std::string::size_type prevPos = 0, pos = 0;

		pos = buffer.find('|', pos);
		string portString(buffer.substr(prevPos, pos - prevPos));
		prevPos = ++pos;

		pos = buffer.find('|', pos);
		string typeString(buffer.substr(prevPos, pos - prevPos));
		prevPos = ++pos;

		pos = buffer.find('|', pos);
		string pixelAmountString(buffer.substr(prevPos, pos - prevPos));
		prevPos = ++pos;

		StripePortMapping mapping;
		mapping.pixelAmount = atoi(pixelAmountString.c_str());
		mapping.port = atoi(portString.c_str());
		mapping.protocollType = typeString;

		stripePortMapping[mapping.port] = mapping;

		writeLine(workingControlSocket, RESPONSE_OK);
	}
	return stripePortMapping;
}

string CotrolSocketHandling::readLine(int &socked) {
	ssize_t rc;
	char c;
	string buffer = "";

	do {
		if ((rc = read(socked, &c, +1)) == 1) {
			buffer.append(&c,1);
			if (c == '\n') {
				break;
			}
		}
	} while (rc > 0);
	return buffer;
}

void CotrolSocketHandling::writeLine(int socked, string &message) {
	send(socked, message.c_str(), message.size(), 0);
}

int CotrolSocketHandling::getCommandIntValue(string &inputString, string commandName) {
	printf("int called %s.\n", inputString.c_str());
	fflush(stdout);
	printf("and command name is %s.\n", commandName.c_str());
	fflush(stdout);

	if (inputString.find(commandName) == string::npos) {
		printf("no int found\n");
		fflush(stdout);
		return -1;
	}

	size_t valueStartPosition = inputString.rfind("=");

	if (valueStartPosition != string::npos) {
		printf("int found\n");
		fflush(stdout);
		string stringResult = inputString.substr(valueStartPosition + 1, inputString.size() - 1);
		printf("int result is %s.\n", stringResult.c_str());
		fflush(stdout);

		int result = atoi(stringResult.c_str());
		return result;
	}
	printf("no int found at end\n");
	fflush(stdout);
	return -1;
}

string CotrolSocketHandling::getCommandStringValue(string &inputString, string commandName) {

	printf("string called %s .\n", inputString.c_str());
	fflush(stdout);

	if (inputString.find(commandName) == string::npos) {
		printf("nothing found\n");
		fflush(stdout);
		return string();
	}

	size_t valueStartPosition = inputString.rfind("=");

	if (valueStartPosition != string::npos) {
		printf("something found\n");
		fflush(stdout);
		return inputString.substr(valueStartPosition + 1, inputString.size() - 1);
	}
	printf("string called returned NUL");
	fflush(stdout);

	return NULL;
}

