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

package org.ambient.roomservice.socketcallback;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.ambient.roomservice.RoomConfigService;


/**
 * @author Florian Bornkessel
 * 
 */
public class CallbackSocketServerRunnable implements Runnable {

	public ServerSocket server = null;
	private RoomConfigService service;
	private boolean run = true;


	public CallbackSocketServerRunnable(RoomConfigService service) {
		super();
		this.service = service;
		try {
			server = new ServerSocket(4321);
		} catch (IOException e) {
			System.out.println("Could not listen on port 4321");
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		try {
			while (run = true) {
				Socket client = server.accept();
				CallbackSocketRunnable serverRunnable = new CallbackSocketRunnable(client, service);
				new Thread(serverRunnable).run();
			}
		} catch (IOException e) {
			if (run != false) {
				// when closing an waiting socket we get allways an exception.
				System.out.println("Accept failed: 4321 " + e.getMessage());
			}

		}
	}


	public void stop() throws IOException {
		run = false;
		server.close();
	}
}
