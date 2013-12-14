/*
 * MaxDispatcherModule.h
 *
 *  Created on: 27.10.2013
 *      Author: florian
 */

#ifndef MAXDISPATCHERMODULE_H_
#define MAXDISPATCHERMODULE_H_

#include "../DispatcherModule.h"
#include "../RFMDispatcher.h"
#include "../../queue/OutMessage.h"

class OutMessage;

class MaxDispatcherModule: public DispatcherModule {
public:
	MaxDispatcherModule(RFMDispatcher *rfmDispatcher) :
			DispatcherModule(rfmDispatcher) {
	};
	virtual ~MaxDispatcherModule();

	bool init(RF22 *rf22);
	int sendMessage(RF22 *rf22, OutMessage message);
	void receiveMessage(RF22 *rf22);
	void switchToTX(RF22 *rf22);
	void switchToRx(RF22 *rf22);

private:
	const RF22::ModemConfig maxModemConfig = { 0x01, //F Filter Coefficient Sets. Defaults are for Rb = 40 kbps and Fd = 20 kHz so Bw = 80 kHz - resetvalue
			0x03, //Clock Recovery Slow Gearshift Value.
			0x90, //oversampling rate
			0x20, //nco1
			0x51, //nco2
			0xeC, //nco3
			0x10, //cr loop gain1
			0x58, //cr loop gain2
			/* 2c - 2e are only for OOK */
			0x00, 0x00, 0x00, 0x80, /* Copied from RF22 defaults */
			0x60, // pga gain override 21db
			0x51,  //datarate 1 15:8
			0xEC,  //datarate 2 7:0 == 10kbps
			0x20, // datarate below 30kbps.
			RF22_DTMOD_FIFO | RF22_MODTYP_FSK, //fifo fsk as it sais. for me: direct mode via spi pin is possible to set here
			0x1e, //frequence derivation 18750
			};

	const uint8_t maxSyncWords[4] = { 0xc6, 0x26, 0xc6, 0x26 };

	const uint8_t incommingMessageLength=30;

	bool sendLongPreamble(RF22 *rf22, bool longPreamble);

	time_t lastMessageOnAir;

};

#endif /* MAXDISPATCHERMODULE_H_ */
