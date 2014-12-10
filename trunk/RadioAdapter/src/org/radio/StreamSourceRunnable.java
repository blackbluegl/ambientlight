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

	private static final int RETRY_COUNT = 100;
	OutputStream intoTranscoderStream;
	String requestUrl;

	HttpURLConnection conn = null;
	DataInputStream serverInputDataStream = null;

	boolean debug = false;

	boolean run = true;


	public StreamSourceRunnable(OutputStream intoTranscoderStream, String requestUrl, boolean debug) {
		super();
		this.intoTranscoderStream = intoTranscoderStream;
		this.requestUrl = requestUrl;
		this.debug = debug;
	}


	@Override
	public void run() {
		try {
			connect(requestUrl);
			while (run) {
				int readByte = 0;
				try {
					readByte = serverInputDataStream.readUnsignedByte();
				} catch (Exception e) {
					if (reconnect()) {
						readByte = serverInputDataStream.readUnsignedByte();
					} else {
						// signal transcoder that there will be no more data
						intoTranscoderStream.close();
						// stop listening to server
						break;
					}
				}
				// maybe during a reconnect this thread was supposed to stop. if so we cannot write into streams that may not
				// exist anymore. so only write into if this thread should run.
				if (run) {
					intoTranscoderStream.write(readByte);
				}
			}
		} catch (Exception e) {
			// an exception occours if transcoder process was finished. In this case we finish here
			System.out.println("RadioAdapter: could not connect to server or transcoder died. Connection to server closed.");
		} finally {
			disconnect();
			System.out.println("RadioAdapter: connection to server closed");
		}
	}


	// reconnect forever until this runnable will be stopped by client
	private boolean reconnect() {
		int i = 0;
		while (run && i < RETRY_COUNT) {
			i++;
			if (debug) {
				System.out.println("RadioAdapter: reconnecting to server");
			}
			disconnect();
			try {
				connect(requestUrl);
				return true;
			} catch (Exception e) {
				try {
					Thread.sleep(5000);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		return false;
	}


	public void stop() {
		run = false;
	}


	private void disconnect() {
		try {
			serverInputDataStream.close();
		} catch (Exception e) {
			// do nothing here
		}
		conn.disconnect();
	}


	private void connect(final String requestUrl) throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL(requestUrl);
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		serverInputDataStream = (new DataInputStream(conn.getInputStream()));
	}

}
