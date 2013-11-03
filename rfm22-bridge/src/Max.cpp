#include <iostream>
#include "rfm22/RF22.h"
#include "socket/Correlation.h"
#include "dispatcher/RFMDispatcher.h"
#include "queue/QeueManager.h"
#include "socket/ServerSocket.h"


int main(int argc, const char* argv[]) {

	if (argc == 2) {
		char *endptr;
		int port = strtol(argv[1], &endptr, 0);
		if (*endptr) {
			fprintf(stderr, "Invalid portNumber.\n");
			exit (EXIT_FAILURE);
		}
		RF22 rf22(0, 0);
		Correlation correlation;
		RFMDispatcher dispatcher(&rf22);
		QeueManager queues(&correlation, &dispatcher);
		dispatcher.queueManager = &queues;
		ServerSocket socketServer(&correlation,&queues);
		socketServer.listenForMessages(port);

		dispatcher.initRFM22();
	}
}

