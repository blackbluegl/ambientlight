/*
 * Correlation.cpp
 *
 *  Created on: 26.10.2013
 *      Author: florian
 */

#include "Correlation.h"

Correlation::Correlation() {
}

Correlation::~Correlation() {
}

SocketHandler* Correlation::getSocketForID(string correlatorId) {

	if(correlationCorrelator.find(correlatorId)==correlationCorrelator.end()){
		return NULL;
	}
	int socketId = correlationCorrelator.find(correlatorId)->second;

	if (correlationMapSocketHandler.find(socketId) == correlationMapSocketHandler.end()) {
		return NULL;
	}
	return correlationMapSocketHandler.find(socketId)->second;
}

void Correlation::registerSocket(SocketHandler *socketHandler, vector<string> correlatorIds) {
	for (unsigned int i = 0; i < correlatorIds.size(); i++) {
		correlationCorrelator.insert(pair<string, int>(correlatorIds.at(i), socketHandler->socketId));
	}

	correlationMapSocketHandler.insert(pair<int,SocketHandler*>(socketHandler->socketId,socketHandler));
}

void Correlation::unregisterSocket(int socket) {
	vector<string> eraseEntries;

	for (map<string, int>::iterator it = correlationCorrelator.begin(); it != correlationCorrelator.end(); ++it) {
		if (it->second == socket) {
			eraseEntries.push_back(it->first);
		}
	}

	for (unsigned int i = 0; i < eraseEntries.size(); i++) {
		correlationCorrelator.erase(eraseEntries.at(i));
	}

	correlationMapSocketHandler.erase(socket);
}

