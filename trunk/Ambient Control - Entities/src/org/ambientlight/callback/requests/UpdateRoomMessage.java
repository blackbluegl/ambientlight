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

package org.ambientlight.callback.requests;

/**
 * @author Florian Bornkessel
 * 
 */
public class UpdateRoomMessage {

	private final static String DELIMITER = "|";
	private final static String REQ_COMMAND_UPDATE_ROOM_CONFIG = "UPDATE_ROOM_CONFIG";


	public static String createMessage(String roomName) {
		return REQ_COMMAND_UPDATE_ROOM_CONFIG + DELIMITER + roomName;
	}


	public static String getFromMessage(String message) {
		if (message.startsWith(REQ_COMMAND_UPDATE_ROOM_CONFIG) == false)
			return null;
		String[] result = message.split(DELIMITER);
		if (result.length != 2)
			return null;
		if (result[1].isEmpty())
			return null;
		return result[1];
	}
}
