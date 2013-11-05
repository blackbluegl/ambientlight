/*
 * ServerSocket.cpp
 *
 *  Created on: 29.10.2013
 *      Author: florian
 */

#include "ServerSocket.h"
#include "SocketHandler.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <string>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <iostream>

ServerSocket::ServerSocket(Correlation *correlation, QeueManager *queues) {
	this->correlation = correlation;
	this->queueManager = queues;
}

ServerSocket::~ServerSocket() {
}

void ServerSocket::listenForMessages(int portNumber) {

	int serverSocked, socketHandlerSocked;
	socklen_t clilen;

	struct sockaddr_in serv_addr, cli_addr;

	serverSocked = socket(AF_INET, SOCK_STREAM, 0);
	if (serverSocked < 0) {
		error("ERROR opening socket");
	}
	bzero((char *) &serv_addr, sizeof(serv_addr));
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = INADDR_ANY;
	serv_addr.sin_port = htons(portNumber);
	if (bind(serverSocked, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0)
		error("ERROR on binding");

	if (listen(serverSocked, 1024) < 0) {
		error("Error calling listen()");
	}

	clilen = sizeof(cli_addr);
	//for now on we wait forever
	while (true) {
		socketHandlerSocked = accept(serverSocked, (struct sockaddr *) &cli_addr, &clilen);
		if (socketHandlerSocked < 0)
			error("ERROR on accept");

		SocketHandler* socketHandler = new SocketHandler(this->correlation, this->queueManager, socketHandlerSocked);

		pthread_attr_t tattr;
		/* set the thread detach state */
		pthread_attr_setdetachstate(&tattr, PTHREAD_CREATE_DETACHED);

		pthread_t createThread;
		pthread_create(&createThread, NULL, SocketHandler::handleCommands, socketHandler);
	}

	close(serverSocked);
}

void ServerSocket::error(const char *msg) {
	perror(msg);
	exit(1);
}
