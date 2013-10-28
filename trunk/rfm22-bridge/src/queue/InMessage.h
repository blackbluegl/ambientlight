/*
 * InMessage.h
 *
 *  Created on: 26.10.2013
 *      Author: florian
 */

#ifndef INMESSAGE_H_
#define INMESSAGE_H_

#include <stdint.h>
#include <string>
#include <vector>
#include "OutMessage.h"

class InMessage {
public:
	InMessage();
	virtual ~InMessage();

	std::string correlation;
	DispatcherType dispatchTo;
	std::vector<uint8_t> payload;

};

#endif /* INMESSAGE_H_ */
