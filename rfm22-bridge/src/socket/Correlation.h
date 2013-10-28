/*
 * Correlation.h
 *
 *  Created on: 26.10.2013
 *      Author: florian
 */

#ifndef CORRELATION_H_
#include <string>
#include <map>

using namespace std;
#define CORRELATION_H_

class Correlation {
public:
	Correlation();

	int getSocketForID(string correlatorId);
	void registerSocket(int socket, string correlatorId);
	void unregisterSocket(int socket);

	virtual ~Correlation();

private:
	map<string,int> correlation;
};

#endif /* CORRELATION_H_ */
