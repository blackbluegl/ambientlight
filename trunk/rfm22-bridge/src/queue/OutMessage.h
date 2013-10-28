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

enum DispatcherType {
	MAX, ELRO
};

class OutMessage {
public:
	OutMessage();
	virtual ~OutMessage();
	DispatcherType dispatchTo;
	std::vector< uint8_t >  getPayLoad();

private:
	std::vector<uint8_t> payload;
};

#endif /* OUTMESSAGE_H_ */
