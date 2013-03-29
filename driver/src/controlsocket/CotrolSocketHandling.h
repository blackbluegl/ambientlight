/*
 * cotrolsockethandling.h
 *
 *  Created on: 28.11.2012
 *      Author: florian
 */

#ifndef COTROLSOCKETHANDLING_H_

#include <unistd.h>             /*  for ssize_t data type  */
#include <stdbool.h>
#include <string>
#include <map>

using namespace std;

#define LISTENQ        (1024)   /*  Backlog for listen()   */

#define COTROLSOCKETHANDLING_H_

class CotrolSocketHandling {
public:
	CotrolSocketHandling();
	virtual ~CotrolSocketHandling();
	string readLine(int &socked);
	map<int,int> handleControlRequests(int workingControlSocket);
	int getCommandValue(string &inputString, string commandName);
	void writeLine(int socked, string &message);
};
#endif /* COTROLSOCKETHANDLING_H_ */
