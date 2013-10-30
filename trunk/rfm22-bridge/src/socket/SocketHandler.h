/*
 * SocketHandler.h
 *
 *  Created on: 27.10.2013
 *      Author: florian
 */

#ifndef SOCKETHANDLER_H_
#define SOCKETHANDLER_H_


#include "../queue/InMessage.h"
#include <vector>
#include <string>
#include "../queue/QeueManager.h"

class Correlation;
class QueueManager;

class SocketHandler {
public:
	SocketHandler(Correlation *correlation, QeueManager *queues, int sockedId);
	virtual ~SocketHandler();
	int sendMessage(InMessage message);
	int socketId;

private:
	Correlation *correlation;
	QeueManager *queueManager;
	std::string readLine(int socked);
	std::vector<uint8_t> readPayload(int socked, unsigned int length);

	void handleRFMMessage(Enums::DispatcherType dispatcherType, std::string commandValues);
};

#endif /* SOCKETHANDLER_H_ */
