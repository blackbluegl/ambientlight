/*
 * DispatcherModule.h
 *
 *  Created on: 27.10.2013
 *      Author: florian
 */

#ifndef DISPATCHERMODULE_H_
#define DISPATCHERMODULE_H_

#include "../rfm22/RF22.h"
#include "../queue/OutMessage.h"
#include "RFMDispatcher.h"

class RFMDispatcher;
class RF22;

class DispatcherModule {
public:
	DispatcherModule(RFMDispatcher *rfmDispatcher);
	virtual ~DispatcherModule();
	virtual bool init(RF22 *rf22);
	virtual int sendMessage(RF22 *rf22, OutMessage message);
	virtual void receiveMessage(RF22 *rf22);
	virtual void switchToTX(RF22 *rf22);
	virtual void switchToRx(RF22 *rf22);

protected:
	RFMDispatcher *dispatcher;
};

#endif /* DISPATCHERMODULE_H_ */
