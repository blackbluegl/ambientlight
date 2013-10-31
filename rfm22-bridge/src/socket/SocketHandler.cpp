/*
 * SocketHandler.cpp
 *
 *  Created on: 27.10.2013
 *      Author: florian
 */

#include "SocketHandler.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <string>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <vector>
#include "../queue/Enums.h"
#include "../queue/CommandMessage.h"
#include "../queue/OutMessage.h"

SocketHandler::SocketHandler(Correlation *correlation, QeueManager *queues, int sockedId) {
	this->correlation = correlation;
	this->queueManager = queues;
	this->socketId = socketId;
	while (true) {
		std::string commandValues = readLine(socketId);

		std::string::size_type prevPos = 0, pos = 0;

		pos = commandValues.find('|', pos);
		std::string command(commandValues.substr(prevPos, pos - prevPos));
		Enums::MessageCommandType commandType = Enums::stringToMessageCommandTypeEnum(command);
		prevPos = ++pos;

		if (commandType == Enums::UNKNOWN_COMMAND) {
			send(socketId, "NOK", 3, 0);
			continue;
		}

		pos = commandValues.find('|', pos);
		std::string typeString(commandValues.substr(prevPos, pos - prevPos));
		Enums::DispatcherType dispatcherType = Enums::stringToEnum(typeString);
		prevPos = ++pos;

		if (commandType == Enums::RFM_SEND_MESSAGE) {
			this->handleRFMMessage(dispatcherType, commandValues.substr(prevPos));
		}
	}
}

SocketHandler::~SocketHandler() {
}

void SocketHandler::handleRFMMessage(Enums::DispatcherType dispatcherType, std::string commandValues) {
	std::string::size_type prevPos = 0, pos = 0;
	pos = commandValues.find('|', pos);
	std::string lengthString(commandValues.substr(prevPos, pos - prevPos));
	int length = atoi(lengthString.c_str());
	OutMessage outMessage;
	outMessage.payLoad = readPayload(this->socketId, length);
	outMessage.dispatchTo = dispatcherType;
	this->queueManager->postOutMessage(outMessage);

	send(socketId, "OK", 2, 0);
}

void SocketHandler::handleRegisterCorrelation(Enums::DispatcherType dispatcherType, std::string commandValues) {
	std::string::size_type prevPos = 0, pos = 0;
	pos = commandValues.find('|', pos);
	std::string correlation(Enums::enumToString(dispatcherType)+"_"+commandValues.substr(prevPos, pos - prevPos));

	this->correlation->registerSocket(this,correlation);
	send(socketId, "OK", 2, 0);
}

void SocketHandler::handleUnRegisterCorrelation(Enums::DispatcherType dispatcherType, std::string commandValues) {
	send(socketId, "OK", 2, 0);
}

void SocketHandler::handleCloseConnection(Enums::DispatcherType dispatcherType, std::string commandValues) {
	send(socketId, "OK", 2, 0);
}

std::string SocketHandler::readLine(int socked) {
	ssize_t rc;
	char c;
	std::string buffer = "";

	do {
		if ((rc = read(socked, &c, +1)) == 1) {
			buffer.append(&c);
			if (c == '\n') {
				break;
			}
		}
	} while (rc > 0);

	if (rc < 0) {
		//error("error reading message");
	}
	return buffer;
}

std::vector<uint8_t> SocketHandler::readPayload(int socked, unsigned int length) {
	ssize_t rc;

	uint8_t data[length];
	std::vector<uint8_t> result;
	rc = read(socked, data, length);

	if (rc < 0) {
		//error("error reading message");
	}

	for (unsigned int i = 0; i < length; i++) {
		result.push_back(data[i]);
	}

	return result;
}

int SocketHandler::sendMessage(InMessage message) {

	std::string dispatchMessage = Enums::enumToString(message.dispatchTo) + "|";
	for (unsigned int i = 0; i < message.payload.size(); i++) {
		dispatchMessage += message.payload.at(i);
	}

	return send(socketId, dispatchMessage.c_str(), dispatchMessage.size(), 0);
}

