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

package org.ambientlight.messages.max;

/**
 * @author Florian Bornkessel
 * 
 */
public class MaxShutterContactStateMessage extends MaxMessage {

	public MaxShutterContactStateMessage() {
		payload = new byte[11];
		setMessageType(MaxMessageType.SHUTTER_CONTACT_STATE);
	}


	public boolean hadRfError() {
		return (((payload[10] & 0xFF) >> 6) & 0x01) > 0 ? true : false;
	}


	public boolean isOpen() {
		return (payload[10] & 0x03) > 0 ? true : false;
	}


	public boolean isBatteryLow() {
		return ((payload[10] & 0xFF) >> 7) > 0 ? true : false;
	}


	@Override
	public String toString() {
		String parent = super.toString();
		String current = new String("Open: " + isOpen() + "\nRfError: " + hadRfError() + "\nBatteryLow: " + isBatteryLow());
		return parent + "\n" + current;
	}
}
