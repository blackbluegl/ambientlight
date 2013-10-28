/*
 * SocketHandler.h
 *
 *  Created on: 27.10.2013
 *      Author: florian
 */

#ifndef SOCKETHANDLER_H_
#define SOCKETHANDLER_H_

#include "Correlation.h"
#include "../queue/InMessage.h"

class SocketHandler {
public:
	SocketHandler(Correlation correlationTable);
	virtual ~SocketHandler();
	void sendMessage(InMessage message);

private:
	Correlation correlation;
};

#endif /* SOCKETHANDLER_H_ */
