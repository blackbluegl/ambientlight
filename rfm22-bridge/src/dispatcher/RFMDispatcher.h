/*
 * RFMDispatcher.h
 *
 *  Created on: 27.10.2013
 *      Author: florian
 */

#ifndef RFMDISPATCHER_H_
#define RFMDISPATCHER_H_

//#include "../queue/OutMessage.h"
#include "../queue/QeueManager.h"
#include "DispatcherModule.h"
#include "../queue/InMessage.h"
#include "../rfm22/RF22.h"
#include "../queue/Enums.h"
#include <map>
class QeueManager;
class DispatcherModule;
class RF22;
class OutMessage;

class RFMDispatcher {
public:
	RFMDispatcher(QeueManager *queues, RF22 *rfm22);
	virtual ~RFMDispatcher();

	void dispatchOutMessage(OutMessage message);
	void dispatchInMessage(InMessage message);
private:
	const Enums::DispatcherType defaultDispatcher = Enums::MAX;

	QeueManager *queueManager;
	Enums::DispatcherType lastDispatcher;
	std::map<Enums::DispatcherType, DispatcherModule> dispatchers;
	RF22 *rfm22;

	void initRFM22();
};

#endif /* RFMDISPATCHER_H_ */
