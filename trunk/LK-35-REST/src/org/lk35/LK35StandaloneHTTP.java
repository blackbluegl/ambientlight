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

package org.lk35;

import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import org.lk35.api.LK35DeviceHandler;
import org.lk35.api.LK35DeviceHandlerImpl;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;


/**
 * The standalone Rest Service for the LK35 API.
 * <p>
 * You may start the server with this command:
 * <code>java -jar lk35rest.jar</code>
 * <p>
 * 
 * You can provide two parameters <code>binding=[ip]:[port]</code> and
 * <code>server=[serverNameOrIP]:[port]</code> Use
 * <code>0.0.0.0<code> for a binding to all local ip addresses.
 * <p>
 * 
 * example: <code>java -jar lk35rest.jar binding=0.0.0.0:1234
 * server=10.0.100.254:8899 </code>
 * 
 * @author Florian Bornkessel
 * 
 */
public class LK35StandaloneHTTP {

	public static final String defaultBinding = "0.0.0.0:8899";

	public static String binding = defaultBinding;
	public static String serverAdress = "10.0.100.254:8899";

	public static OutputStream sessionOutputStream = null;

	private static LK35DeviceHandler device = null;

	private static boolean connecctionInProgress = false;


	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
		parseArguments(args);

		// init connection to Server
		device = new LK35DeviceHandlerImpl();

		// conecting to LK35
		connect(false);

		// add handle for ctrl+c to disconnect from lk device
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				try {
					System.out.println("disconnecting from server");
					device.disconnect();
				} catch (IOException e) {
					// we cannot do much here.
					e.printStackTrace();
				}
			}
		});

		// Start Webservice
		final String BASE_URI = "http://" + binding + "/rest";
		final ResourceConfig rc = new PackagesResourceConfig("org.lk35.rest");
		rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
		rc.getFeatures().put("com.sun.jersey.config.feature.Trace", true);

		GrizzlyServerFactory.createHttpServer(BASE_URI, rc);

		// wait for external signal to stop
		System.out.println("press ctrl+c to stop server");
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}


	private static void parseArguments(String[] args) {
		// parse arguments
		if (args.length > 0) {
			for (String currentArg : args) {
				if (currentArg.contains("binding")) {
					StringTokenizer st = new StringTokenizer(currentArg, "=");
					st.nextToken();
					binding = st.nextToken();
				}
				if (currentArg.contains("server")) {
					StringTokenizer st = new StringTokenizer(currentArg, "=");
					st.nextToken();
					serverAdress = st.nextToken();
				}
			}
		}
	}


	public static void connect(boolean reconnect) {
		// if a thread was starting the reconnection progress we can stop right
		// here
		if (connecctionInProgress) {
			System.out.println("reconnecting to LK35 will be ommitted because another process is trying to connect.");
			return;
		}

		connecctionInProgress = true;
		if (reconnect) {
			System.out.println("reconnecting to LK35");
			try {
				device.disconnect();
			} catch (Exception e) {
				System.out.println("error while trying to disconnect from device. continuing anyway");
				e.printStackTrace();
			}
		} else {
			System.out.println("connecting to LK35");
		}

		boolean reconnected = false;

		while (reconnected == false) {
			try {
				StringTokenizer st = new StringTokenizer(LK35StandaloneHTTP.serverAdress, ":");
				sessionOutputStream = device.connect(st.nextToken(), Integer.parseInt(st.nextToken()));
				reconnected = true;
				System.out.println("connection established.");
				connecctionInProgress = false;
			} catch (Exception e) {
				System.out.println("(re)connection failed. Waiting for 10 seconds.");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					// we cannot do much here.
				}
			}
		}
	}
}
