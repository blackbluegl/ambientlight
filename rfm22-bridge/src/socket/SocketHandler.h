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
	int socketId;
	static void* handleOutMessagesWrap(void* arg);
	virtual ~SocketHandler();
	int sendMessage(InMessage message);


private:
	Correlation *correlation;
	QeueManager *queueManager;
	std::string readLine(int socked);
	std::vector<uint8_t> readPayload(int socked, unsigned int length);

	void handleRFMMessage(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues);
	void handleRegisterCorrelation(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues);
	void handleUnRegisterCorrelation(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues);
	void handleCloseConnection(Enums::DispatcherType dispatcherType, std::vector<std::string> commandValues);

	std::vector<std::string> getValuesOfMessage(std::string commandValues);




};

#endif /* SOCKETHANDLER_H_ */
