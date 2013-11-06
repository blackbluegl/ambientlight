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
#include <sstream>
#include "SockedException.h"

SocketHandler::SocketHandler(Correlation *correlation, QeueManager *queues, int socketId) {
	this->correlation = correlation;
	this->queueManager = queues;
	this->socketId = socketId;
	this->correlation->registerSocket(this);
}

SocketHandler::~SocketHandler() {
}

void* SocketHandler::handleCommands(void* arg) {
	SocketHandler* myself = (SocketHandler*) arg;

	try {
		while (true) {

			std::vector<std::string> commandValues = myself->getValuesOfMessage(myself->readLine(myself->socketId));

			if (commandValues.size() < 1) {
				if (send(myself->socketId, "NOK", 3, MSG_NOSIGNAL) < 0) {
					SockedException ex;
					throw ex;
				}
				continue;
			}

			Enums::MessageCommandType commandType = Enums::stringToMessageCommandTypeEnum(commandValues.at(0));

			if (commandType == Enums::UNKNOWN_COMMAND) {
				if (send(myself->socketId, "NOK", 3, MSG_NOSIGNAL) < 0) {
					SockedException ex;
					throw ex;
				}
				continue;
			}

			if (commandType == Enums::CLOSE_CONNECTION) {
				handleCloseConnection(myself);
				return 0;
			}

			Enums::DispatcherType dispatcherType = Enums::stringToEnum(commandValues.at(1));

			if (commandType == Enums::RFM_SEND_MESSAGE) {
				myself->handleRFMMessage(dispatcherType, commandValues);
			} else if (commandType == Enums::REGISTER_CORRELATION) {
				myself->handleRegisterCorrelation(dispatcherType, commandValues);
			} else if (commandType == Enums::UNREGISTER_CORRELATION) {
				myself->handleUnRegisterCorrelation(dispatcherType, commandValues);
			}
		}
	} catch (SockedException &e) {
		cout << "SocketHandler handleCommands(): caught sockedException!\n";
		handleCloseConnection(myself);
	}

	return 0;
}

void SocketHandler::handleRFMMessage(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues) {

	int length = atoi(commandValues.at(3).c_str());
	OutMessage outMessage;
	outMessage.payLoad = readBytes(this->socketId, length);
	outMessage.dispatchTo = dispatcherType;
	this->queueManager->postOutMessage(outMessage);

	if (send(socketId, "OK", 2, 0) < 0) {
		SockedException ex;
		throw ex;
	}
}

void SocketHandler::handleRegisterCorrelation(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues) {
	std::string correlation(Enums::enumToString(dispatcherType) + "_" + commandValues.at(2));
	this->correlation->registerCorrelation(this, correlation);
	if (send(socketId, "OK", 2, 0) < 0) {
		SockedException ex;
		throw ex;
	}
}

void SocketHandler::handleUnRegisterCorrelation(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues) {
	std::string correlation(Enums::enumToString(dispatcherType) + "_" + commandValues.at(2));
	this->correlation->unRegisterCorrelation(this, correlation);
	if (send(socketId, "OK", 2, 0) < 0) {
		SockedException ex;
		throw ex;
	}
}

void SocketHandler::handleCloseConnection(SocketHandler* socketHandler) {
	cout << "SocketHandler handleCloseConnection(): closing connection\n";
	send(socketHandler->socketId, "OK", 2, MSG_NOSIGNAL);
	close(socketHandler->socketId);
	socketHandler->correlation->unregisterSocket(socketHandler->socketId);
	delete socketHandler;
}

std::string SocketHandler::readLine(int socked) {
	ssize_t rc;
	char c;
	std::string buffer = "";
	do {
		if ((rc = read(socked, &c, +1)) == 1) {
			buffer.append(&c,1);
			if (c == '\n') {
				break;
			}
		}
	} while (rc > 0);

	if (rc < 0) {
		cout << "SocketHandler readLine(): error reading socked!\n";
		SockedException ex;
		throw ex;
	}
	cout << "SocketHandler readLine(): " << buffer << "\n";
	return buffer;
}

std::vector<uint8_t> SocketHandler::readBytes(int socked, unsigned int length) {
	ssize_t rc;

	uint8_t data[length];
	std::vector<uint8_t> result;
	rc = read(socked, data, length);

	if (rc < 0) {
		cout << "SocketHandler readBytes(): error reading socked!\n";
		SockedException ex;
		throw ex;
	}

	for (unsigned int i = 0; i < length; i++) {
		result.push_back(data[i]);
	}

	return result;
}

void SocketHandler::handleInMessage(InMessage message) {

	std::stringstream fromStream;
	fromStream << message.payload.size();
	std::string size = fromStream.str();
	std::string dispatchMessage = Enums::enumToString(message.dispatchTo) + "|" + size + "|" + "\n";
	for (unsigned int i = 0; i < message.payload.size(); i++) {
		dispatchMessage += message.payload.at(i);
	}

	if (send(socketId, dispatchMessage.c_str(), dispatchMessage.size(), MSG_NOSIGNAL) < 0) {
		SockedException ex;
		throw ex;
	}
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
