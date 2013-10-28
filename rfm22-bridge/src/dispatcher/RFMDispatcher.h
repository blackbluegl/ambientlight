/*
 * RFMDispatcher.h
 *
 *  Created on: 27.10.2013
 *      Author: florian
 */

#ifndef RFMDISPATCHER_H_
#define RFMDISPATCHER_H_

#include "../queue/OutMessage.h"
#include "../queue/QeueManager.h"
#include "DispatcherModule.h"
#include "../queue/InMessage.h"
#include "../rfm22/RF22.h"
class QeueManager;
class DispatcherModule;
class RF22;

class RFMDispatcher {
public:
	RFMDispatcher(QeueManager *queues);
	virtual ~RFMDispatcher();

	void dispatchOutMessage(OutMessage message);
	void dispatchInMessage(InMessage message);
private:
	const DispatcherType defaultDispatcher = MAX;

	QeueManager *queueManager;
	DispatcherType lastDispatcher;
	std::map<DispatcherType, DispatcherModule> dispatchers;
	//RF22 rfm22;

	void initRFM22();
};

#endif /* RFMDISPATCHER_H_ */
