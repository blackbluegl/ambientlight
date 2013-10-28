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

int Correlation::getSocketForID(string correlatorId) {

	if (correlation.find(correlatorId) == correlation.end()) {
		return -1;
	}
	return correlation.find(correlatorId)->second;
}

void Correlation::registerSocket(int socket, string correlatorId) {
	correlation.insert(pair<string, int>(correlatorId, socket));
}

void Correlation::unregisterSocket(int socket) {

	string correlator ="";
	  for (map<string,int>::iterator it=correlation.begin(); it!=correlation.end(); ++it){
		    if( it->second==socket){
		    	correlator= it->first;
		    }
	  }
	  if(correlator==""){
		  return;
	  }
	  correlation.erase(correlator);
}

