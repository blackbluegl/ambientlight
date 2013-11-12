/*
 * QeueManager.cpp
 *
 *  Created on: 27.10.2013
 *      Author: florian
 */

#include "QeueManager.h"
#include <pthread.h>
#include "../socket/SocketHandler.h"
#include "../dispatcher/RFMDispatcher.h"
#include "../socket/SockedException.h"
#include <iostream>

pthread_mutex_t mutexLockOutQueue = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t conditionOutQueueFilled = PTHREAD_COND_INITIALIZER;

pthread_mutex_t mutexLockInQueue = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t conditionInQueueFilled = PTHREAD_COND_INITIALIZER;

QeueManager::QeueManager(Correlation *correlation, RFMDispatcher *rfmDispatcher) {
	this->correlation = correlation;
	dispatcher = rfmDispatcher;
}

QeueManager::~QeueManager() {
}

void* QeueManager::handleOutMessagesWrap(void* arg) {
	((QeueManager*) arg)->handleOutMessages();
	return 0;
}

void QeueManager::handleOutMessages() {
	while (true) {
		pthread_mutex_lock(&mutexLockOutQueue);
		pthread_cond_wait(&conditionOutQueueFilled, &mutexLockOutQueue);
		while (outQeue.size() > 0) {
			OutMessage outMessage = outQeue.at(0);
			outQeue.erase(outQeue.begin());
			dispatcher->dispatchOutMessage(outMessage);
		}
		pthread_mutex_unlock(&mutexLockOutQueue);
	}
}

void QeueManager::postOutMessage(OutMessage message) {
	pthread_mutex_lock(&mutexLockOutQueue);
	outQeue.push_back(message);
	pthread_cond_signal(&conditionOutQueueFilled);
	pthread_mutex_unlock(&mutexLockOutQueue);
}

void* QeueManager::handleInMessagesWrap(void* arg) {
	((QeueManager*) arg)->handleInMessages();
	return 0;
}

void QeueManager::handleInMessages() {
	while (true) {
		pthread_mutex_lock(&mutexLockInQueue);
		pthread_cond_wait(&conditionInQueueFilled, &mutexLockInQueue);

		while (inEnqueueAt != inReadAt) {

			inReadAt++;
			if(inReadAt==100){
				inReadAt=0;
			}

			InMessage msg = inQeue[inReadAt];
			SocketHandler *socketHandler = this->correlation->getSocketForID(msg.correlation);
			try {
				if (socketHandler != NULL) {
					socketHandler->handleInMessage(msg);
				} else {
					for (std::map<int, SocketHandler*>::iterator it = this->correlation->correlationMapSocketHandler.begin();
							it != this->correlation->correlationMapSocketHandler.end(); ++it) {
						it->second->handleInMessage(msg);
					}
				}
			} catch (SockedException &ex) {
				cout << "QueueManager handleInMessages(): sockedHandler has no valid connection anymore. removal should be applied by the corresponding thread";
			}
		}
		pthread_mutex_unlock(&mutexLockInQueue);
	}
}

void QeueManager::postInMessage(InMessage message) {
	inEnqueueAt++;
	if (inEnqueueAt == 100) {
		inEnqueueAt = 0;
	}
	inQeue[inEnqueueAt] = message;
	//create a thread that informs the outQueue to send new Data
	pthread_t informThread;
	pthread_create(&informThread, NULL, QeueManager::informInQueueFilledWrap, this);
}

void* QeueManager::informInQueueFilledWrap(void* arg) {
	((QeueManager*) arg)->informInQueueFilled();
	return 0;
}

void QeueManager::informInQueueFilled() {
	pthread_mutex_lock(&mutexLockInQueue);
	pthread_cond_signal(&conditionInQueueFilled);
	pthread_mutex_unlock(&mutexLockInQueue);
}

void QeueManager::startQeues() {
	pthread_t outThread, inThread;
	pthread_create(&outThread, NULL, QeueManager::handleOutMessagesWrap, this);
	pthread_create(&inThread, NULL, QeueManager::handleInMessagesWrap, this);
}
