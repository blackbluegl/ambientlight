/*
 * QeueManager.h
 *
 *  Created on: 27.10.2013
 *      Author: florian
 */

#ifndef QEUEMANAGER_H_
#define QEUEMANAGER_H_

#include "OutMessage.h"
#include "InMessage.h"
#include "../dispatcher/RFMDispatcher.h"
#include "../socket/Correlation.h"
#include <vector>
#include <map>
#include "../socket/SocketHandler.h"

class RFMDispatcher;

class Correlation;


class QeueManager {
public:
	QeueManager(Correlation *correlation, RFMDispatcher *dispatcher);
	virtual ~QeueManager();

	void startQeues();

	void postOutMessage(OutMessage message);
	void postInMessage(InMessage message);

private:
	Correlation *correlation;
	RFMDispatcher *dispatcher;

	std::vector<OutMessage> outQeue;
	InMessage inQeue[100];
	int inEnqueueAt = 0;
	int inReadAt = 0;

	static void* handleOutMessagesWrap(void* arg);
	static void* handleInMessagesWrap(void* arg);
	static void* informInQueueFilledWrap(void* arg);
	void handleOutMessages();
	void handleInMessages();
	void informInQueueFilled();

};

#endif /* QEUEMANAGER_H_ */
