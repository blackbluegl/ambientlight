/*
 * Enums.h
 *
 *  Created on: 30.10.2013
 *      Author: florian
 */

#ifndef ENUMS_H_
#define ENUMS_H_
#include <string>



class Enums {

public:
	Enums();
	virtual ~Enums();

	enum DispatcherType {
		MAX, ELRO, UNKNOWN
	};

	enum MessageCommandType {
		REGISTER_CORRELATION, UNREGISTER_CORRELATION, RFM_SEND_MESSAGE, UNKNOWN_COMMAND
	};

	static MessageCommandType stringToMessageCommandTypeEnum(std::string command);
	static std::string enumToString(Enums::DispatcherType dispatchType);
	static DispatcherType stringToEnum(std::string dispatchType);
};

#endif /* ENUMS_H_ */
