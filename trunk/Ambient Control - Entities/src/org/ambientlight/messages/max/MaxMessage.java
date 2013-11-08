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

	public byte[] payload;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.Message#getDispatcherType()
	 */
	@Override
	public DispatcherType getDispatcherType() {
		return DispatcherType.MAX;
	}


	public int getFromAdress(){
		if (payload.length < 7)
			return 0;
		return byteArrayToInt(payload, 3, 3);
	}


	public int getToAdress() {
		if (payload.length < 10)
			return 0;
		return byteArrayToInt(payload, 6, 3);
	}


	public MaxMessageType getMessageType(){
		if (payload.length < 3)
			return MaxMessageType.UNKNOWN;
		else
			return MaxMessageType.forCode(payload[2]);
	}

	@Override
	public String toString() {
		return "MaxMessage: " + getMessageType() + " from: " + getFromAdress() + " to: " + getToAdress();

	}

	public static int byteArrayToInt(byte[] b, int offset, int length) {
		int value = 0;
		for (int i = 0; i < length; i++) {
			int shift = (length - 1 - i) * 8;
			value += (b[i + offset] & 0x000000FF) << shift;
		}
		return value;
	}


	public static byte[] intToByteArray(int a) {
		byte[] ret = new byte[4];
		ret[3] = (byte) (a & 0xFF);
		ret[2] = (byte) ((a >> 8) & 0xFF);
		ret[1] = (byte) ((a >> 16) & 0xFF);
		ret[0] = (byte) ((a >> 24) & 0xFF);
		return ret;
	}
}
