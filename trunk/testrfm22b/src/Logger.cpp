#include "Logger.h"
#include <stdlib.h>
#include <sys/time.h>
#include <vector>
#include <fstream>
#include <wiringPi.h>
#include <iostream>
#include <string>
#include "MaxRF22.h"

#include <sstream>

struct timeval tv;
struct timezone tz;

Logger* Logger::logInstance;

Logger::Logger(MaxRF22 *rfm, unsigned long samples) {
	startTime=0;
	rf=rfm;
	maxCount = samples;
	logInstance = this;
	states = std::vector<int> (maxCount);
	timestampsUSec = std::vector<__suseconds_t>(maxCount) ;
	timestampsSec= std::vector<__time_t>(maxCount) ;
}

void Logger::loop() {
	gettimeofday(&tv, &tz);
	__time_t  timestamp_seconds = tv.tv_sec;
	startTime=timestamp_seconds;
	delay(1000);
	rf->spiWrite(RF22_REG_0D_GPIO_CONFIGURATION2, 0x13);
	delay(1000);
	wiringPiISR(5, INT_EDGE_BOTH, Logger::isr0);
	delay(1000);
	rf->spiWrite(RF22_REG_0D_GPIO_CONFIGURATION2, 0x14); //raw data to port 4

	while (counter < maxCount) {
		delay(300);
		std::cout << "waiting for more samples. got already: " << counter <<"\n";
	}
	std::cout << "writing file to disk\n";
	std::ofstream myfile;
	myfile.open("log.tab");
	myfile << "timecode data\n";

	for(unsigned long i=0;i<maxCount;i++){
		long int timeInSecods = timestampsSec.at(i)-startTime;
		float timeInUSeconds = (float)timestampsUSec.at(i)/1000000;
		float timeStamp  = timeInUSeconds+timeInSecods;
		std::ostringstream timeStampStream;
		timeStampStream << timeStamp;
		std::string timeStampString(timeStampStream.str());
		int value = states.at(i);

		myfile << timeStampString <<" "<<value;
	//	if(i+1<maxCount){
			myfile << "\n";
	//	}
	}
	myfile.close();
	rf->spiWrite(RF22_REG_0D_GPIO_CONFIGURATION2, 0x13);

}

void Logger::handleInterrupt() {
	if (counter < maxCount) {
		gettimeofday(&tv, &tz);
		__suseconds_t timestamp = tv.tv_usec;
		__time_t  timestamp_seconds = tv.tv_sec;
		int state = digitalRead(5);
		states.at(counter)=state;
		timestampsUSec.at(counter)=timestamp;
		timestampsSec.at(counter)=timestamp_seconds;
		counter++;
	} else {


	}

}

void Logger::isr0() {
	logInstance->handleInterrupt();
}

