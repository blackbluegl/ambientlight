/*
 * ServerSocket.h
 *
 *  Created on: 29.10.2013
 *      Author: florian
 */

#ifndef SERVERSOCKET_H_
#define SERVERSOCKET_H_
#include <string>
#include "../queue/QeueManager.h"

class Correlation;

class ServerSocket {

public:
	ServerSocket(Correlation *correlation, QeueManager *queues);
	virtual ~ServerSocket();

	void listenForMessages(int portNumber);

private:

	Correlation *correlation;
	QeueManager *queueManager;

	void error(const char *msg);

};

#endif /* SERVERSOCKET_H_ */
