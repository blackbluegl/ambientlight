/*  Copyright 2013 Florian Bornkessel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.ambientlight.rfmbridge;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.ambientlight.config.messages.DispatcherConfiguration;
import org.ambientlight.rfmbridge.messages.PingMessage;


/**
 * These dispatchers also waits for messages from the rfm22 bridge. Therefore it may parse incomming messages and it has a
 * heartbeat check to find out if a socket is still alive and useable or not. Note the rfm-bridge uses its correlation mechanism
 * for message routing. This dispatcher receives messages with an registered correlator or all broadcasted messages where no
 * correlator was found in the rfm-bridge.
 * 
 * @author Florian Bornkessel
 * 
 */
public abstract class InDispatcher extends Dispatcher {

	/**
	 * if we wait for messages we have to take care that the socket up running correctly. Therefore a ping is send and the
	 * dispatcher should get an pong. If the blocking read got an pong the variable will be true and the heartbeat check passes
	 * without a reconnect.
	 */
	private boolean pongReceived = false;


	/**
	 * @param configuration
	 * @param queueManager
	 */
	public InDispatcher(DispatcherConfiguration configuration, QeueManager queueManager) {
		super(configuration, queueManager);
	}


	/**
	 * check the connection by sending a ping and waiting for pong to become true from the parseInMessage() method.
	 * 
	 * @return connection state
	 */
	public boolean checkConnection() {

		// this variable should be set to true during the sleep.
		pongReceived = false;

		// send ping and give a chance for an answer
		try {
			deliverOutMessage(new PingMessage());
			Thread.sleep(10000);
			if (pongReceived)
				return true;
		} catch (Exception e) {
			return false;
		}
		return false;
	}


	/**
	 * receive messages. The format should be: {dispatcherType}|{payLoadLength}\n{payLoad}
	 * 
	 * @return a message object
	 * @throws IOException
	 */
	public Message receiveMessages() throws IOException {
		InputStream in = socket.getInputStream();

		// wait until a complete line arrives
		String line = readLine(in);

		String[] command = line.split("\\|");
		// the message is only valid if both header elements are present
		if (command.length != 2)
			return null;

		// extract dispatcher type
		String dispatcherType = command[0];

		// read payload
		int length = Integer.parseInt(command[1]);
		byte[] messageBytes = new byte[length];
		in.read(messageBytes, 0, length);

		return parseMessage(dispatcherType, messageBytes);
	}


	/**
	 * parse message and handle "pong" answer. All other message types will be delegated to concrete dispatchers.
	 * 
	 * @param dispatcherType
	 * @param input
	 * @return
	 */
	private Message parseMessage(String dispatcherType, byte[] input) {

		// handle system messages
		if ("SYSTEM".equals(dispatcherType)) {
			char[] chars = new char[input.length];
			for (int i = 0; i < input.length; i++) {
				chars[i] = (char) input[i];
			}
			String payload = new String(chars);
			if ("PONG".equals(payload)) {
				pongReceived = true;
			}
			return null;
		}

		// all other messages will be handled by concrete dispatchers
		if (getDispatcherType().toString().equals(dispatcherType))
			return handleInMessage(input);
		else
			return null;
	}


	/**
	 * parse message in concrete dispatcher type. Note: the dispatcher receives messages wich are correlated to the socket or
	 * which are broadcasted because there is no correlator registered at the rfm22-bridge.
	 * 
	 * @param dispatcherType
	 * @param input
	 * @return
	 */
	public abstract Message handleInMessage(byte[] input);


	/**
	 * helper method to read the header line from messages.
	 * 
	 * @param inputStream
	 * @return the header
	 * @throws IOException
	 */
	private String readLine(InputStream inputStream) throws IOException {
		ArrayList<Character> result = new ArrayList<Character>();

		boolean finished = false;
		while (!finished) {
			int charAt = inputStream.read();
			if (charAt == -1) {
				finished = true;
			} else if ('\n' == (char) charAt) {
				finished = true;
			} else {
				result.add((char) charAt);
			}
		}

		String resultString = new String();
		for (char charAt : result) {
			resultString += charAt;
		}

		return resultString;
	}

}
