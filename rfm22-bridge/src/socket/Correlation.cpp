/*
 * Correlation.cpp
 *
 *  Created on: 26.10.2013
 *      Author: florian
 */

#include "Correlation.h"
#include <iostream>
#include <sstream>

Correlation::Correlation() {
}

Correlation::~Correlation() {
}

SocketHandler* Correlation::getSocketForID(string correlatorId) {

	if (correlationCorrelator.find(correlatorId) == correlationCorrelator.end()) {
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

bool Correlation::unregisterSocket(int sockedId) {

	std::map<int, SocketHandler*>::iterator it;

	it = correlationMapSocketHandler.find(sockedId);
	if(it == correlationMapSocketHandler.end()){
		cout <<"Correlation unregister: socket is not registered: "<<sockedId<<"\n";
		return false;
	}
	cout << "Correlation unregister: unregistering: "<<sockedId<<"\n";

	correlationMapSocketHandler.erase(it);

	vector<string> eraseEntries;

	for (map<string, int>::iterator it = correlationCorrelator.begin(); it != correlationCorrelator.end(); ++it) {
		if (it->second == sockedId) {
			eraseEntries.push_back(it->first);
		}
	}

	for (unsigned int i = 0; i < eraseEntries.size(); i++) {
		correlationCorrelator.erase(eraseEntries.at(i));
	}
	return true;
}

void Correlation::registerSocket(SocketHandler *socketHandler) {

	correlationMapSocketHandler.insert(pair<int, SocketHandler*>(socketHandler->socketId, socketHandler));

}

