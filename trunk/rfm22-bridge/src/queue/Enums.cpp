/*
 * Enums.cpp
 *
 *  Created on: 30.10.2013
 *      Author: florian
 */

#include "Enums.h"
#include <string>

Enums::Enums() {
	// TODO Auto-generated constructor stub

}

Enums::~Enums() {
	// TODO Auto-generated destructor stub
}

Enums::MessageCommandType Enums::stringToMessageCommandTypeEnum(std::string command) {
	if ("REGISTER_CORRELATION" == command) {
		return REGISTER_CORRELATION;
	}
	if ("UNREGISTER_CORRELATION" == command) {
		return UNREGISTER_CORRELATION;
	}
	if ("RFM_SEND_MESSAGE" == command) {
		return RFM_SEND_MESSAGE;
	}
	if ("CLOSE_CONNECTION" == command) {
		return CLOSE_CONNECTION;
	}
	if ("PING" == command) {
		return PING;
	}
	return UNKNOWN_COMMAND;
}

std::string Enums::enumToString(Enums::DispatcherType dispatchType) {
	switch (dispatchType) {
	case Enums::MAX:
		return "MAX";
		break;
	case Enums::ELRO:
		return "ELRO";
		break;
	case Enums::UNKNOWN:
		return "UNKNOWN";
		break;
	case Enums::SYSTEM:
		return "SYTEM";
		break;
	}
	return "UNKNOWN";
}

Enums::DispatcherType Enums::stringToEnum(std::string dispatchType) {
	if ("MAX" == dispatchType) {
		return Enums::MAX;
	}
	if ("ELRO" == dispatchType) {
		return Enums::ELRO;
	}
	if("SYSTEM" == dispatchType){
		return Enums::SYSTEM;
	}
	return Enums::UNKNOWN;
}

