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

import java.util.Arrays;
import java.util.Date;

import org.ambientlight.messages.AckResponseMessage;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxAckMessage extends MaxMessage implements AckResponseMessage {

	public MaxAckMessage() {
		payload = new byte[17];
		setMessageType(MaxMessageType.ACK);
	}


	@Override
	public byte[] getPayload() {
		if (getAckType() == MaxAckType.ACK_SIMPLE || getAckType() == MaxAckType.ACK_INVALID_MESSAGE)
			return Arrays.copyOf(payload, 11);
		return payload;
	}


	public MaxAckType getAckType() {
		return MaxAckType.forCode(payload[10]);
	}


	public void setAckType(MaxAckType type) {
		payload[10] = type.byteValue;
	}


	public MaxThermostateMode getMode() {
		return MaxThermostateMode.forCode(payload[11] & 0x3);
	}


	public boolean getDST() {
		return (payload[11] >> 2 & 0x1) > 0 ? true : false;
	}


	public boolean isLocked() {
		return (payload[11] >> 5 & 0x1) > 0 ? true : false;
	}


	public boolean hadRfError() {
		return (payload[10] >> 6 & 0x1) > 0 ? true : false;
	}


	public boolean isBatteryLow() {
		return (payload[11] >> 7) > 0 ? true : false;
	}


	public int getValvePosition() {
		return payload[12] & 0xff;
	}


	public float getSetTemp() {
		return (payload[13] & 0xff) / 2;
	}


	public Date getTemporaryUntil() {
		if (this.getMode() != MaxThermostateMode.TEMPORARY)
			return null;
		byte[] time = new byte[] { payload[14], payload[15], payload[16] };
		return MaxUtil.parseUntilTime(time);
	}


	@Override
	public String toString() {
		String parent = super.toString() + "\n";
		String myString = "";
		try {
			myString = "Mode: " + getMode() + "\n";
			myString += "AckType: " + getAckType() + "\n";
			myString += "Until: " + getTemporaryUntil() + "\n";
			myString += "DST: " + getDST() + "\n";
			myString += "Locked: " + isLocked() + "\n";
			myString += "BatteryLow: " + hadRfError() + "\n";
			myString += "RF-Error: " + isBatteryLow() + "\n";
			myString += "ValvePos: " + getValvePosition() + "\n";
			myString += "Set Temp: " + getSetTemp() + "\n";
		} catch (Exception e) {
			myString = "Thermostat may not be initialized propperly";
		}
		return parent + myString;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.AckResponseMessage#getCorrelator()
	 */
	@Override
	public String getCorrelator() {
		return String.valueOf(getSequenceNumber());
	}
}
