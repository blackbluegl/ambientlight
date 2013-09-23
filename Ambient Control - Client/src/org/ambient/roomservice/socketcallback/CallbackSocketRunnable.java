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
import org.ambientlight.callback.requests.UpdateRoomMessage;
import org.ambientlight.callback.responses.StatusMessage;


/**
 * @author Florian Bornkessel
 * 
 */
public class CallbackSocketRunnable implements Runnable {

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
			final String request = in.readLine();
			if (UpdateRoomMessage.getFromMessage(request) != null) {
				// extract servername, commit to service
				new Thread(new Runnable() {

					@Override
					public void run() {
						System.out.println("CallBackSocketRunnable: updating RoomConfig for server: "
								+ UpdateRoomMessage.getFromMessage(request));
						service.updateRoomConfigForRoomName(UpdateRoomMessage.getFromMessage(request));
					}
				}).start();

				out.println(StatusMessage.createMessage(true));
			} else {
				out.println(StatusMessage.createMessage(false));
			}
			server.close();
		} catch (IOException ioe) {
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		}
	}

}
