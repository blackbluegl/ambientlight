/*
 * OutMessage.cpp
 *
 *  Created on: 26.10.2013
 *      Author: florian
 */

#include "OutMessage.h"

OutMessage::OutMessage() {
	// TODO Auto-generated constructor stub

}

OutMessage::~OutMessage() {
	// TODO Auto-generated destructor stub
}


std::vector< uint8_t >  OutMessage::getPayLoad(){
	return payload;
}
