/*
 * Rfm12ASK.h
 *
 *  Created on: 31.03.2013
 *      Author: florian
 */

#ifndef RFM12ASK_H_

using namespace std;

#define RFM12ASK_H_

class Rfm12ASK {
public:
	Rfm12ASK();
	virtual ~Rfm12ASK();
	int rf12SetupTx();
	int setupRx();
	void rf12DisableTransmitter();
	void rf12TransmitHigh();
	void rf12EnableTransmitter();
	void rf12TransmitLow();

private:
	void rf12_xfer(unsigned int data);
};


#endif /* RFM12ASK_H_ */
