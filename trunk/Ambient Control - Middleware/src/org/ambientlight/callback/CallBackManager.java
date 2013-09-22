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

package org.ambientlight.callback;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.callback.requests.UpdateRoomMessage;
import org.ambientlight.callback.responses.StatusMessage;


/**
 * @author Florian Bornkessel
 * 
 */
public class CallBackManager {

	List<String> clients = new ArrayList<String>();


	public void roomConfigurationChanged() {
		for (String currentClient : clients) {

			String hostname = currentClient.split(":")[0];
			int port = Integer.parseInt(currentClient.split(":")[1]);

			Socket socket = null;
			PrintWriter out = null;
			BufferedReader in = null;

			try {
				socket = new Socket(hostname, port);
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				out.println(UpdateRoomMessage.createMessage(AmbientControlMW.getRoom().config.roomName));
				System.out.println("CallbackManager: client: " + currentClient + " will be notified");

				if (StatusMessage.getFromMessage(in.readLine()) == false) {
					System.out.println("CallbackManager: client: " + currentClient + " does not accept the message!");
				}

				out.close();
				in.close();
				socket.close();

			} catch (Exception e) {
				System.err.println("CallbackManager: Error notifying client: " + currentClient
						+ ". Continuing without callback message.");
				e.printStackTrace();
			}
		}
	}


	public void registerClient(String ipAndPort) {
		System.out.println("CallbackManager: registering client for callback: " + ipAndPort);
		if (!clients.contains(ipAndPort)) {
			clients.add(ipAndPort);
		}
	}


	public void unregisterClient(String ipAndPort) {
		System.out.println("CallbackManager: unregistering client for callback: " + ipAndPort);
		clients.remove(ipAndPort);
	}
}
