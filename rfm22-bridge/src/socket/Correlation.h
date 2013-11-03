/*
 * Correlation.h
 *
 *  Created on: 26.10.2013
 *      Author: florian
 */

#ifndef CORRELATION_H_
#define CORRELATION_H_
#include <string>
#include <map>
#include "SocketHandler.h"

using namespace std;


class SocketHandler;

class Correlation {
public:
	map<int,SocketHandler*> correlationMapSocketHandler;

	Correlation();
	virtual ~Correlation();

	SocketHandler* getSocketForID(string correlatorId);
	void registerCorrelation(SocketHandler *socketHandler, string correlatorId);
	void unRegisterCorrelation(SocketHandler *socketHandler, string correlatorId);
	void unregisterSocket(int socket);


private:

	map<string,int> correlationCorrelator;
};

#endif /* CORRELATION_H_ */
