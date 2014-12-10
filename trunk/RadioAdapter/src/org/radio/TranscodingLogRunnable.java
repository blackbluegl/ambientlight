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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * @author Florian Bornkessel
 * 
 */
public class TranscodingLogRunnable implements Runnable {

	InputStream inputFromTranscoder = null;

	boolean debug = false;


	public TranscodingLogRunnable(InputStream inputFromTranscoder, boolean debug) {
		super();
		this.inputFromTranscoder = inputFromTranscoder;
		this.debug = debug;
	}


	@Override
	public void run() {
		try {
			BufferedReader error = new BufferedReader(new InputStreamReader(inputFromTranscoder));
			String result = error.readLine();
			while (result != null) {
				if (debug) {
					System.out.println(result);
				}
				result = error.readLine();
			}
			System.out.println("RadioAdapter: console logging stopped because there is no more output from transcoder");
		} catch (Exception e) {
			System.out.println("RadioAdapter: console logging stopped because of an error");
			e.printStackTrace();
		}
	}
}
