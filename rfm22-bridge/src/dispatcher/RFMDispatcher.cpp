/*
 * RFMDispatcher.cpp
 *
 *  Created on: 27.10.2013
 *      Author: florian
 */

#include "RFMDispatcher.h"
#include "max/MaxDispatcherModule.h"
#include <map>
#include <iostream>
#include "../queue/Enums.h"
#include <unistd.h>

RFMDispatcher::RFMDispatcher(RF22 *rfm22) {
	this->rfm22 = rfm22;
	dispatchers.insert(pair<Enums::DispatcherType, DispatcherModule*>(Enums::MAX, new MaxDispatcherModule(this)));
	lastDispatcher = defaultDispatcher;
}

RFMDispatcher::~RFMDispatcher() {
}

void RFMDispatcher::sendADirectResponse(OutMessage response){
	queueManager->postOutMessage(response,true);
}

void RFMDispatcher::dispatchOutMessage(OutMessage message) {
	cout << "RFMDispatcher dispatchOutMessage(): dispatching message of type" << Enums::enumToString(message.dispatchTo) << "\n";
	DispatcherModule* module = dispatchers.at(message.dispatchTo);

	if (lastDispatcher != message.dispatchTo) {
		//dispatchermodule is different. reinit sending module
		module->init(rfm22);
		lastDispatcher = message.dispatchTo;
	}

	module->switchToTX(rfm22);
	module->sendMessage(rfm22, message);

	//now set module to rx
	if (lastDispatcher != defaultDispatcher) {
		//change back to default dispatchermodule
		module = dispatchers.at(defaultDispatcher);
		module->init(rfm22);
		lastDispatcher = defaultDispatcher;
		rfm22->setDispatcherModule(module);
		cout << "RFMDispatcher dispatchOutMessage(): waiting for new messages. changed back to dispatcher type"
				<< Enums::enumToString(defaultDispatcher) << "\n";
	}

	//wait for new messages
	module->switchToRx(rfm22);
	//at least wait a second for incomming messages
	usleep(100000);
}

void RFMDispatcher::dispatchInMessage(InMessage message) {
	queueManager->postInMessage(message);
}

void RFMDispatcher::initRFM22() {
	if (rfm22->init() == false) {
		perror("could not connect to rfm22 module!");
		exit(1);
	}

	DispatcherModule* module = dispatchers.at(defaultDispatcher);
	rfm22->setDispatcherModule(module);
	module->init(rfm22);
	module->switchToRx(rfm22);
}

