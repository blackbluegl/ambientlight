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


class Correlation;
class QeueManager;



class SocketHandler {
public:
	SocketHandler(Correlation *correlation, QeueManager *queues, int socketId);
	virtual ~SocketHandler();
	int socketId;
	static void* handleCommands(void* arg);

	void handleInMessage(InMessage message);
	static void handleCloseConnection(SocketHandler* socketHandler, Correlation* correlation);

private:
	Correlation *correlation;
	QeueManager *queueManager;
	std::string readLine(int socked);
	std::vector<uint8_t> readBytes(int socked, unsigned int length);

	pthread_mutex_t mutexLockCloseSocket;



	void handleRFMMessage(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues);
	void handleRegisterCorrelation(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues);
	void handleUnRegisterCorrelation(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues);
	void handleTest(Enums::DispatcherType dispatcherType);
	void handlePing();
	std::vector<std::string> getValuesOfMessage(std::string commandValues);
	void sendResponse(std::string response);
};

#endif /* SOCKETHANDLER_H_ */
