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
#include "../queue/QeueManager.h"
#include <iostream>

SocketHandler::SocketHandler(Correlation *correlation, QeueManager *queues, int socketId) {
	this->correlation = correlation;
	this->queueManager = queues;
	this->socketId = socketId;
}

SocketHandler::~SocketHandler() {
}

void* SocketHandler::handleOutMessagesWrap(void* arg) {
	SocketHandler* myself = (SocketHandler*) arg;

	while (true) {

		std::vector<std::string> commandValues = myself->getValuesOfMessage(myself->readLine(myself->socketId));

		if (commandValues.size() < 3) {
			send(myself->socketId, "NOK", 3, 0);
			continue;
		}

		Enums::MessageCommandType commandType = Enums::stringToMessageCommandTypeEnum(commandValues.at(0));

		if (commandType == Enums::UNKNOWN_COMMAND) {
			send(myself->socketId, "NOK", 3, 0);
			continue;
		}

		Enums::DispatcherType dispatcherType = Enums::stringToEnum(commandValues.at(1));

		if (commandType == Enums::RFM_SEND_MESSAGE) {
			myself->handleRFMMessage(dispatcherType, commandValues);
		} else if (commandType == Enums::REGISTER_CORRELATION) {
			myself->handleRegisterCorrelation(dispatcherType, commandValues);
		} else if (commandType == Enums::UNREGISTER_CORRELATION) {
			myself->handleUnRegisterCorrelation(dispatcherType, commandValues);
		} else if (commandType == Enums::CLOSE_CONNECTION) {
			myself->handleCloseConnection(dispatcherType, commandValues);
			delete myself;
			return 0;
		}
	}
	return 0;
}

void SocketHandler::handleRFMMessage(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues) {

	int length = atoi(commandValues.at(3).c_str());
	OutMessage outMessage;
	outMessage.payLoad = readPayload(this->socketId, length);
	outMessage.dispatchTo = dispatcherType;
	this->queueManager->postOutMessage(outMessage);

	send(socketId, "OK", 2, 0);
}

void SocketHandler::handleRegisterCorrelation(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues) {
	std::string correlation(Enums::enumToString(dispatcherType) + "_" + commandValues.at(2));
	this->correlation->registerCorrelation(this, correlation);
	send(socketId, "OK", 2, 0);
}

void SocketHandler::handleUnRegisterCorrelation(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues) {
	std::string correlation(Enums::enumToString(dispatcherType) + "_" + commandValues.at(2));
	this->correlation->unRegisterCorrelation(this, correlation);
	send(socketId, "OK", 2, 0);
}

void SocketHandler::handleCloseConnection(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues) {
	this->correlation->unregisterSocket(this->socketId);
	send(socketId, "OK", 2, 0);
	close(socketId);
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
	cout << buffer;
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

std::vector<std::string> SocketHandler::getValuesOfMessage(std::string commandValues) {
	std::vector<std::string> results;
	std::string::size_type prevPos = 0, pos = 0;
	while (pos != std::string::npos) {
		pos = commandValues.find('|', pos);
		if (pos == std::string::npos) {
			break;
		}
		results.push_back(commandValues.substr(prevPos, pos - prevPos));
		prevPos = ++pos;
	}
	return results;
}
