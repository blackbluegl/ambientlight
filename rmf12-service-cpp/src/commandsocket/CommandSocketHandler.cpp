/*
 * CommandSocketHandler.cpp
 *
 *  Created on: 31.03.2013
 *      Author: florian
 */

#include "CommandSocketHandler.h"
#include "../switch/SwitchHandler.h"
#include <sys/socket.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <map>

namespace std {

CommandSocketHandler::CommandSocketHandler() {
	// TODO Auto-generated constructor stub

}

CommandSocketHandler::~CommandSocketHandler() {
	// TODO Auto-generated destructor stub
}

void CommandSocketHandler::handleCommandRequest(int workingControlSocket) {

	string RESPONSE_OK = "OK\n";

	while (1) {
		/* read a line*/
		string buffer;

		/* end if client closed connection or a failure did occour */
		buffer = readLine(workingControlSocket);
		if (buffer.empty()) {
			return;
		}

		//handle switching requests
		string commandValues = getCommandValue(buffer, "switch");
		SwitchHandler swh;
		swh.handleSwitch(commandValues);

		writeLine(workingControlSocket, RESPONSE_OK);
	}
	return;
}

string CommandSocketHandler::readLine(int &socked) {
	ssize_t rc;
	char c;
	string buffer = "";

	do {
		if ((rc = read(socked, &c, +1)) == 1) {
			buffer.append(&c);
			if (c == '\n') {
				break;
			}
		}
	} while (rc > 0);
	return buffer;
}

void CommandSocketHandler::writeLine(int socked, string &message) {
	send(socked, message.c_str(), message.size(), 0);
}

string CommandSocketHandler::getCommandValue(string &inputString, string commandName) {

	if (inputString.find(commandName) == string::npos) {
		return NULL;
	}

	size_t valueStartPosition = inputString.rfind("=");

	if (valueStartPosition != string::npos) {

		return inputString.substr(valueStartPosition + 1, inputString.size() - 1);
	}
	return NULL;
}

}
