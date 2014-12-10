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

package org.radio;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


/**
 * @author Florian Bornkessel
 * 
 */
public class StreamSourceRunnable implements Runnable {

	OutputStream intoTranscoderStream;
	String requestUrl;

	HttpURLConnection conn = null;
	DataInputStream serverInputDataStream = null;

	boolean run = true;


	public StreamSourceRunnable(OutputStream intoTranscoderStream, String requestUrl) {
		super();
		this.intoTranscoderStream = intoTranscoderStream;
		this.requestUrl = requestUrl;
	}


	@Override
	public void run() {
		try {
			connect(requestUrl);
			while (run) {
				intoTranscoderStream.write(serverInputDataStream.readUnsignedByte());
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				intoTranscoderStream.close();
			} catch (IOException e1) {
				// cannot do anything here
			}
		} finally {
			disconnect();
			System.out.println("RadioAdapter: server connection lost");
		}
	}


	public void stop() {
		run = false;
	}


	private void disconnect() {
		try {
			conn.disconnect();
			serverInputDataStream.close();
		} catch (Exception e) {
			// do nothing here
		}
	}


	private void connect(final String requestUrl) throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL(requestUrl);
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		serverInputDataStream = (new DataInputStream(conn.getInputStream()));
	}

}
