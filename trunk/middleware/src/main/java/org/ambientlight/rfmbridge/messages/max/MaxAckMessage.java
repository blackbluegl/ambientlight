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

package org.ambientlight.rfmbridge.messages.max;

import java.util.Arrays;
import java.util.Date;

import org.ambientlight.rfmbridge.ResponseMessage;
import org.ambientlight.room.entities.climate.util.MaxThermostateMode;
import org.ambientlight.room.entities.climate.util.MaxUtil;


/**
 * @author Florian Bornkessel
 */
public class MaxAckMessage extends MaxMessage implements ResponseMessage {

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
		if (payload.length < 12)
			return null;
		return MaxThermostateMode.forCode(payload[11] & 0x3);
	}


	public boolean getDST() {
		if (payload.length < 12)
			return false;
		return (payload[11] >> 2 & 0x1) > 0 ? true : false;
	}


	public boolean isLocked() {
		if (payload.length < 12)
			return false;
		return (payload[11] >> 5 & 0x1) > 0 ? true : false;
	}


	public boolean hadRfError() {
		return (payload[10] >> 6 & 0x1) > 0 ? true : false;
	}


	public boolean isBatteryLow() {
		if (payload.length < 12)
			return false;
		return (payload[11] >> 7) > 0 ? true : false;
	}

	public int getValvePosition() {
		if (payload.length < 13)
			return 0;
		return payload[12] & 0xff;
	}


	public float getSetTemp() {
		if (payload.length < 14)
			return 0.0f;
		return (payload[13] & 0xff) / 2.0f;
	}


	public Date getTemporaryUntil() {
		if (this.getMode() != MaxThermostateMode.TEMPORARY || this.payload.length < 15)
			return null;
		byte[] time = new byte[] { payload[14], payload[15], payload[16] };
		return MaxUtil.parseUntilTime(time);
	}


	@Override
	public String toString() {
		String parent = super.toString() + "\n";
		String myString = "";
		myString += "AckType: " + getAckType() + "\n";

		if (payload.length > 11) {
			myString = "Mode: " + getMode() + "\n";
			myString += "Until: " + getTemporaryUntil() + "\n";
			myString += "DST: " + getDST() + "\n";
			myString += "Locked: " + isLocked() + "\n";
			myString += "BatteryLow: " + hadRfError() + "\n";
			myString += "RF-Error: " + isBatteryLow() + "\n";
			myString += "ValvePos: " + getValvePosition() + "\n";
			myString += "Set Temp: " + getSetTemp() + "\n";
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
