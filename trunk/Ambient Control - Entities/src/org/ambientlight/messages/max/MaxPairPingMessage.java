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
public class MaxPairPingMessage extends MaxMessage {

	// there are two types. a repairing and a pairing

	public MaxPairPingMessage() {
		payload = new byte[23];
		setMessageType(MaxMessageType.PAIR_PING);
	}


	@Override
	public byte[] getPayload() {
		return payload;
	}


	public boolean isReconnecting() {
		if (super.getToAdress().equals(0))
			return true;
		else
			return false;
	}


	public String getFirmware() {
		int firmware = getPayload()[10] & 0xFF;
		int major = firmware >> 0x04;
		int minor = firmware & 0x0F;
		return major + "." + minor;
	}


	public DeviceType getDeviceType() {
		return DeviceType.forCode(getPayload()[11]);
	}


	public boolean isValid() {
		return (payload[12] & 0xFF) == 0xFF ? true : false;
	}


	public String getSerial() {
		StringBuilder result = new StringBuilder();
		for (int i = 13; i < payload.length; i++) {
			result.append((char) payload[i]);
		}
		return result.toString();
	}


	@Override
	public String toString() {
		String parent = super.toString();
		String current = "isReconnecting: " + isReconnecting() + "\nfirmware: " + getFirmware()
				+ "\nDeviceType: " + getDeviceType() + "\nisValid: " + isValid() + "\nSerial: " + getSerial();
		return parent + "\n" + current;
	}
}
