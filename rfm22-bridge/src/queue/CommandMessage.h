/*
 * CommandMessage.h
 *
 *  Created on: 30.10.2013
 *      Author: florian
 */

#ifndef COMMANDMESSAGE_H_
#define COMMANDMESSAGE_H_

#include "Enums.h"

class CommandMessage {
public:
	CommandMessage();
	virtual ~CommandMessage();
	Enums::MessageCommandType commandType;
	Enums::DispatcherType dispatchTo;
};

#endif /* COMMANDMESSAGE_H_ */
