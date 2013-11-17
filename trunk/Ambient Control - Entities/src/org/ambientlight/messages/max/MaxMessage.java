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

import org.ambientlight.messages.DispatcherType;
import org.ambientlight.messages.Message;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxMessage extends Message {

	public static int MAX_SEQUENCE_NUMBER = 255;

	public static final int FLAGS_NONE = 0x0;
	public static final int FLAG_REQUEST = 0x4;
	public static final int FLAG_0X1 = 0x1;
	public static final int FLAG_RESPONSE = 0x2;

	protected byte[] payload = new byte[10];


	public byte[] getPayload() {
		return payload;
	}


	public void setPayload(byte[] payload) {
		this.payload = payload;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.Message#getDispatcherType()
	 */
	@Override
	public DispatcherType getDispatcherType() {
		return DispatcherType.MAX;
	}


	public Integer getFromAdress() {
		if (payload.length < 7)
			return null;
		return MaxUtil.byteArrayToInt(payload, 3, 3);
	}


	public void setFromAdress(int adress) {
		byte[] result = MaxUtil.intToByteArray(adress);
		payload[3] = result[1];
		payload[4] = result[2];
		payload[5] = result[3];
	}


	public Integer getToAdress() {
		if (payload.length < 10)
			return null;
		return MaxUtil.byteArrayToInt(payload, 6, 3);
	}


	public void setToAdress(int adress) {
		byte[] result = MaxUtil.intToByteArray(adress);
		payload[6] = result[1];
		payload[7] = result[2];
		payload[8] = result[3];
	}


	public MaxMessageType getMessageType() {
		if (payload.length < 3)
			return MaxMessageType.UNKNOWN;
		else
			return MaxMessageType.forCode(payload[2]);
	}


	public void setMessageType(MaxMessageType messageType) {
		payload[2] = messageType.byteValue;
	}


	public Integer getSequenceNumber() {
		if (payload.length < 1)
			return null;
		return payload[0] & 0xFF;
	}


	public void setSequenceNumber(int seqNumber) throws IllegalArgumentException {
		if (seqNumber > MAX_SEQUENCE_NUMBER)
			throw new IllegalArgumentException("sequenceNumber is greater MAX_SEQUENCE_NUMBER");
		payload[0] = (byte) seqNumber;
	}


	public Integer getFlags() {
		if (payload.length < 1)
			return null;
		return payload[1] & 0xFF;
	}


	public void setFlags(int flags) {
		payload[1] = (byte) flags;
	}


	public Integer getGroupNumber() {
		if (payload.length < 10)
			return null;
		return payload[9] & 0xFF;
	}


	// public void setGroupNumber(int groupNumber) {
	// payload[9] = (byte) groupNumber;
	// }

	@Override
	public String toString() {
		return "MaxMessage: " + getMessageType() + " - SeqNr: " + getSequenceNumber() + " from: " + getFromAdress() + " to: "
				+ getToAdress() + " with groupId: " + getGroupNumber() + " Flags: 0x" + Integer.toHexString(getFlags())
				+ "\nPayload: " + MaxUtil.getHexString(payload);
	}

}
