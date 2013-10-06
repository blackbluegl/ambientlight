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

package org.ambientlight.callback.responses;

/**
 * @author Florian Bornkessel
 * 
 */
public class StatusMessage {

	public final static String RES_OK = "OK";
	public final static String RES_UNKNOWN_COMMAND = "UNKNOWN_COMMAND";


	public static String createMessage(boolean ok) {
		if (ok)
			return RES_OK;
		else
			return RES_UNKNOWN_COMMAND;
	}


	public static boolean getFromMessage(String message) {
		if (RES_OK.equals(message))
			return true;
		return false;
	}
}
