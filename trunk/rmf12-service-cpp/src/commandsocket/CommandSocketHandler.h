/*
 * CommandSocketHandler.h
 *
 *  Created on: 31.03.2013
 *      Author: florian
 */

#ifndef COMMANDSOCKETHANDLER_H_

#include <unistd.h>             /*  for ssize_t data type  */
#include <stdbool.h>
#include <string>
#include <map>

#define COMMANDSOCKETHANDLER_H_

namespace std {

class CommandSocketHandler {
public:
	CommandSocketHandler();
	virtual ~CommandSocketHandler();
	void handleCommandRequest(int workingControlSocket);
	string readLine(int &socked);
	map<int,int> handleControlRequests(int workingControlSocket);
	string getCommandValue(string &inputString, string commandName);
	void writeLine(int socked, string &message);
};

} /* namespace std */
#endif /* COMMANDSOCKETHANDLER_H_ */
