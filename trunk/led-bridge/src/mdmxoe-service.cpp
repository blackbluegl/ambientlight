//============================================================================
// Name        : mdmxoe-service-cpp.cpp
// Author      : 
// Version     :
// Copyright   : 
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include <sys/socket.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <unistd.h>
#include "controlsocket/CotrolSocketHandling.h"
#include "datasocket/DataSocketHandling.h"
#include "datasocket/StripePortMapping.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
using namespace std;

#define DEFAULT_PORT  (2002)

short int get_port_to_listen(int argc, char *argv[]) {
	short int port;

	if (argc == 2) {
		char *endptr;
		port = strtol(argv[1], &endptr, 0);
		if (*endptr) {
			fprintf(stderr, "mdmxoe-service: Invalid port number.\n");
			exit(EXIT_FAILURE);
		}
		printf("using given port %d\n", port);
		fflush(stdout);

	} else if (argc < 2) {
		port = DEFAULT_PORT;
		printf("using default port %d \n", DEFAULT_PORT);
		fflush(stdout);

	} else {
		fprintf(stderr, "mdmxoe-service: Invalid arguments.\n");
		exit(EXIT_FAILURE);
	}

	return port;
}

int main(int argc, char *argv[]) {

	int listening_socket;
	int controlSocket;
	int working_data_socket;

	struct sockaddr_in servaddr;

	printf("Starting up\n");
	fflush(stdout);

	short int port = get_port_to_listen(argc, argv);

	/* Create the listening socket */
	if ((listening_socket = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
		fprintf(stderr, "mdmxoe-service: Error creating listening socket.\n");
		exit(EXIT_FAILURE);
	}

	/* Set all bytes in socket address structure to zero, and fill in the relevant data members */
	memset(&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = htonl(INADDR_ANY );
	servaddr.sin_port = htons(port);

	/* Bind our socket addresss to the listening socket, and call listen() */
	if (bind(listening_socket, (struct sockaddr *) &servaddr, sizeof(servaddr)) < 0) {
		fprintf(stderr, "mdmxoe-service: Error calling bind()\n");
		exit(EXIT_FAILURE);
	}

	if (listen(listening_socket, LISTENQ) < 0) {
		fprintf(stderr, "mdmxoe-service: Error calling listen()\n");
		exit(EXIT_FAILURE);
	}

	/* Enter an infinite loop to respond to client requests and echo input */
	while (1) {

		/* Wait for a connection, then accept() it */
		if ((controlSocket = accept(listening_socket, NULL, NULL)) < 0) {
			fprintf(stderr, "mdmxoe-service: Error calling accept for setup\n");
			exit(EXIT_FAILURE);
		}

		// initialize the runtime parameter - this will be closed after client continues to datahandling
		CotrolSocketHandling cth;
		map<int, StripePortMapping> stripePortMapping = cth.handleControlRequests(controlSocket);

		if (stripePortMapping.size() > 0) {
			printf("Accepted new client with %u configured stripes. Waiting for data.\n", (int)stripePortMapping.size());
			fflush(stdout);

			//wait for datas to be send
			if ((working_data_socket = accept(listening_socket, NULL, NULL)) < 0) {
				fprintf(stderr, "mdmxoe-service: Error calling accept for data\n");
				exit(EXIT_FAILURE);
			}

			//handle datas as long this socket is open
			DataSocketHandling dsh;
			dsh.handleDataRequests(working_data_socket, stripePortMapping);
			printf("finishing receiving data\n");

			/*  Close the connected socket  */
			if (close(working_data_socket) < 0) {
				fprintf(stderr, "ECHOSERV: Error calling close()\n");
				exit(EXIT_FAILURE);
			}
		} else {
			printf("Client could not be accepted. Configuration handling could not be finished properly.\n");
			fflush(stdout);
		}

		/*  Close the connected control socket  */
		if (close(controlSocket) < 0) {
			fprintf(stderr, "ECHOSERV: Error calling close()\n");
			exit(EXIT_FAILURE);
		}
		printf("closing control socket and waiting for new client.\n");
		fflush(stdout);
	}
}
