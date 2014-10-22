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
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.ambientlight.config.messages.DispatcherConfiguration;
import org.ambientlight.config.messages.DispatcherType;


/**
 * dispatcher with Connection management and message conversion to the rfm22-bridge.
 * 
 * @author Florian Bornkessel
 * 
 */
public abstract class Dispatcher {

	protected DispatcherConfiguration configuration;
	protected QeueManager queueManager;

	protected Socket socket;


	public Dispatcher(DispatcherConfiguration configuration, QeueManager queueManager) {
		super();
		this.configuration = configuration;
		this.queueManager = queueManager;
	}


	public abstract DispatcherType getDispatcherType();


	/**
	 * delivers messages. Message format: {command}|{dispatcherType}|{payloadSize}|\n{payload} or
	 * {command}|{dispatcherType}|{value}|\n
	 * 
	 * @see org.ambientlight.messages.OutDispatcher#deliverPayLoad(org.ambientlight.rfmbridge.Message)
	 */
	public synchronized boolean deliverOutMessage(Message message) {
		try {
			// command and dispatcher type
			String header = message.getCommand() + "|" + message.getDispatcherType() + "|";
			// size of the payload is stored in the value field
			if (message.getValue() != null) {
				header = header + message.getValue() + "|";
			}
			// end of header
			header = header + "\n";

			// write header
			PrintStream ps = new PrintStream(socket.getOutputStream());
			ps.print(header);
			ps.flush();

			// write payload
			if (message.getDispatcherType().equals(this.configuration.type)) {
				deliverPayLoad(message);
			}
		} catch (Exception e) {
			System.out.println("MaxDispatcher deliverMessage(): was unable to send data!");
			return false;
		}
		return true;
	}


	/**
	 * some dispatcher send additional payload here. Therefore the value field of the message represents the length of the binary
	 * payload for the rfm22-bridge.
	 * 
	 * @param message
	 * @throws IOException
	 */
	protected abstract void deliverPayLoad(Message message) throws IOException;


	/**
	 * create connection and store socket as global field.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void connect() throws UnknownHostException, IOException {
		socket = new Socket(this.configuration.hostName, this.configuration.port);
	}


	/**
	 * close the connection and remove the socket reference in the dispatcher.
	 */
	void closeConnection() {
		try {
			socket.close();
			socket = null;
		} catch (Exception e) {
			System.out.println("Dispatcher closeConnection(): error while closing connection: ");
		}
	}
}
