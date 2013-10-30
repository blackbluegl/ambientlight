/*
 * OutMessage.h
 *
 *  Created on: 26.10.2013
 *      Author: florian
 */

#ifndef OUTMESSAGE_H_
#define OUTMESSAGE_H_

#include <stdint.h>
#include <vector>
#include <string>
#include "Enums.h"



class OutMessage {
public:
	OutMessage();
	virtual ~OutMessage();
	Enums::DispatcherType dispatchTo;
	std::vector<uint8_t> payLoad;
private:

};

#endif /* OUTMESSAGE_H_ */
