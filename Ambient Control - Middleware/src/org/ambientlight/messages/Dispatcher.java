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

package org.ambientlight.messages;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.locks.ReentrantLock;

import org.ambientlight.device.drivers.RemoteHostConfiguration;


/**
 * @author Florian Bornkessel
 * 
 */
public abstract class Dispatcher {

	public ReentrantLock sendLock = new ReentrantLock();

	public RemoteHostConfiguration configuration;
	public QeueManager queueManager;
	protected Socket socket;


	public abstract boolean isConnected();


	public abstract boolean deliverMessage(Message message);


	void connect() throws UnknownHostException, IOException {
		socket = new Socket(this.configuration.hostName, this.configuration.port);
	}


	public boolean reconnect() {
		try {
			closeConnection();
			socket = new Socket(this.configuration.hostName, this.configuration.port);
		} catch (Exception e) {
			return false;
		}
		return true;
	}


	void closeConnection() {
		try {
			socket.close();
		} catch (Exception e) {
			System.out.println("Dispatcher closeConnection(): error while closing connection: ");
		}
	}
}
