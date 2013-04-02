/*
 * SwitchHandler.h
 *
 *  Created on: 31.03.2013
 *      Author: florian
 */

#ifndef SWITCHHANDLER_H_
#include <string>
#define SWITCHHANDLER_H_

namespace std {

class SwitchHandler {
public:
	SwitchHandler();
	virtual ~SwitchHandler();
	void handleSwitch(string commandValues);
private:
	void sendPowerCode(int code);
	void sendHouseCode(int housecode);
	void sendSwitchCode(int number);
	void sendF();
	void send0();
	void sendSync();
	void microSleep(int microsec);
	void handleElroSwitch(int houseCode,int switchNumber,int powerState);
};

} /* namespace std */
#endif /* SWITCHHANDLER_H_ */
