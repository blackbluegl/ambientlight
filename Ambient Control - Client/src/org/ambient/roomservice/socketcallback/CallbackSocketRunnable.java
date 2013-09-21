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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import org.ambient.roomservice.RoomConfigService;


/**
 * @author Florian Bornkessel
 * 
 */
public class CallbackSocketRunnable implements Runnable {

	public final static String delimiter = "|";
	public final static String REQ_COMMAND_UPDATE_ROOM_CONFIG = "UPDATE_ROOM_CONFIG";

	public final static String RES_OK = "OK";
	public final static String RES_UNKNOWN_COMMAND = "UNKNOWN_COMMAND";

	private Socket server = null;
	private RoomConfigService service;


	public CallbackSocketRunnable(Socket server, RoomConfigService service) {
		this.server = server;
		this.service = service;
	}


	@Override
	public void run() {

		try {
			// Get input from the client
			BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
			PrintStream out = new PrintStream(server.getOutputStream());
			String request = in.readLine();
			if (request.contains(REQ_COMMAND_UPDATE_ROOM_CONFIG)) {
				// extract servername, commit to service
				String serverNameAndPort = request.split(delimiter)[1];
				service.updateRoomConfigFor(serverNameAndPort);
				out.println(RES_OK);
			} else {
				out.println(RES_UNKNOWN_COMMAND);
			}
			server.close();
		} catch (IOException ioe) {
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		}
	}

}
