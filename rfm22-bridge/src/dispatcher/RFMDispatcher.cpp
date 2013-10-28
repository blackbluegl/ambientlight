/*
 * RFMDispatcher.cpp
 *
 *  Created on: 27.10.2013
 *      Author: florian
 */

#include "RFMDispatcher.h"
#include "max/MaxDispatcherModule.h"

RF22 rfm22(0, 0);

RFMDispatcher::RFMDispatcher(QeueManager *queues) {
	this->queueManager = queues;
	dispatchers.insert(pair<DispatcherType, DispatcherModule>(MAX,MaxDispatcherModule(this)));
	lastDispatcher=defaultDispatcher;
}

RFMDispatcher::~RFMDispatcher() {
}

void RFMDispatcher::dispatchOutMessage(OutMessage message) {
	DispatcherModule module = dispatchers.at(message.dispatchTo);

	if (lastDispatcher != message.dispatchTo) {
	//dispatchermodule is different. reinit sending module
		module.init(&rfm22);
		lastDispatcher=message.dispatchTo;
	}

	module.switchToTX(&rfm22);
	module.sendMessage(&rfm22,message);

	//now set module to rx
	if(lastDispatcher!=defaultDispatcher){
		//change back to default dispatchermodule
		module=dispatchers.at(defaultDispatcher);
		module.init(&rfm22);
		lastDispatcher=defaultDispatcher;
	}

	//wait for new messages
	module.switchToRx(&rfm22);
}

void RFMDispatcher::dispatchInMessage(InMessage message) {
	queueManager->postInMessage(message);
}

void RFMDispatcher::initRFM22() {
	rfm22.init();
}

