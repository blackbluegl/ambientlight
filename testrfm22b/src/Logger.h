#ifndef Logger_h
#define Logger_h
#include <stdint.h>
#include <vector>
#include <sys/time.h>
class MaxRF22;


class Logger {
public:
	unsigned long maxCount = 0;
	volatile unsigned long counter = 0;
	__time_t startTime = 0;
	MaxRF22 *rf;

	std::vector<int> states;
	std::vector<__suseconds_t> timestampsUSec;
	std::vector<__suseconds_t> timestampsSec;

	Logger(MaxRF22 *rfm, unsigned long samples);
	void loop();
	void handleInterrupt();

protected:
	static void isr0();

	static Logger* logInstance;
};

#endif 
