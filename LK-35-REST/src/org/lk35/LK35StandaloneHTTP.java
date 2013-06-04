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

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;


/**
 * @author Florian Bornkessel
 * 
 */
public class LK35StandaloneHTTP {

	public static final String defaultBinding = "0.0.0.0:8899";

	public static String binding = defaultBinding;
	public static String serverAdress = "10.0.100.254:8899";

	public static OutputStream sessionOutputStream = null;


	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
		parseArguments(args);

		// init connection to Server
		final LK35DeviceHandler device = new LK35DeviceHandlerImpl();

		StringTokenizer st = new StringTokenizer(LK35StandaloneHTTP.serverAdress, ":");
		System.out.println("connecting to server");
		sessionOutputStream = device.connect(st.nextToken(), Integer.parseInt(st.nextToken()));

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

}
