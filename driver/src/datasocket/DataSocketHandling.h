/*
 * DataSocketHandling.h
 *
 *  Created on: 28.11.2012
 *      Author: florian
 */

#ifndef DATASOCKETHANDLING_H_

#include <map>
#include <unistd.h>

using namespace std;

#define DATASOCKETHANDLING_H_

class DataSocketHandling {
public:
	DataSocketHandling();
	virtual ~DataSocketHandling();
	void handleDataRequests(int &workingControlSocket, map<int,int> &stripePortMapping);
	ssize_t readData(int sockd, void *vptr, size_t maxlen);
};

#endif /* DATASOCKETHANDLING_H_ */
