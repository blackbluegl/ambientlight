/*
 * DispatcherModule.cpp
 *
 *  Created on: 27.10.2013
 *      Author: florian
 */

#include "DispatcherModule.h"
DispatcherModule::DispatcherModule(RFMDispatcher *rfmDispatcher) {
	dispatcher = rfmDispatcher;
}

DispatcherModule::~DispatcherModule() {

}

bool DispatcherModule::init(RF22 *rf22) {
	return true;
}
void DispatcherModule::sendMessage(RF22 *rf22, OutMessage message) {

}
void DispatcherModule::receiveMessage(RF22 *rf22) {

}
void DispatcherModule::switchToTX(RF22 *rf22) {

}
void DispatcherModule::switchToRx(RF22 *rf22) {

}

