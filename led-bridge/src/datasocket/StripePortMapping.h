/*
 * StripePortMapping.h
 *
 *  Created on: 05.05.2013
 *      Author: florian
 */

#ifndef STRIPEPORTMAPPING_H_
#include <string>

using namespace std;

#define STRIPEPORTMAPPING_H_


class StripePortMapping {
public:
	StripePortMapping();
	virtual ~StripePortMapping();
	int port;
	int pixelAmount;
	string protocollType;

};

#endif /* STRIPEPORTMAPPING_H_ */
