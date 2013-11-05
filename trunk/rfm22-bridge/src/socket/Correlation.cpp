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

void Correlation::registerCorrelation(SocketHandler *socketHandler, string correlatorId) {

	correlationCorrelator.insert(pair<string, int>(correlatorId, socketHandler->socketId));
}

void Correlation::unRegisterCorrelation(SocketHandler *socketHandler, string correlatorId) {
	correlationCorrelator.erase(correlatorId);
}

void Correlation::unregisterSocket(int sockedId) {

	correlationMapSocketHandler.erase(sockedId);

	vector<string> eraseEntries;

	for (map<string, int>::iterator it = correlationCorrelator.begin(); it != correlationCorrelator.end(); ++it) {
		if (it->second == sockedId) {
			eraseEntries.push_back(it->first);
		}
	}

	for (unsigned int i = 0; i < eraseEntries.size(); i++) {
		correlationCorrelator.erase(eraseEntries.at(i));
	}

}

void Correlation::registerSocket(SocketHandler *socketHandler) {

	correlationMapSocketHandler.insert(pair<int, SocketHandler*>(socketHandler->socketId,socketHandler));

}

