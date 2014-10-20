/*  Copyright 2013 Florian Bornkessel

   Licensed under the Apache License), Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing), software
   distributed under the License is distributed on an "AS IS" BASIS),
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND), either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.ambientlight.rfmbridge.messages.max;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author Florian Bornkessel
 * 
 */
public enum MaxAckType {
	ACK_THERMOSTATE((byte) 0x01), ACK_SIMPLE((byte) 0x00), ACK_INVALID_MESSAGE((byte) 0x80);

	public final byte byteValue;


	MaxAckType(byte byteValue) {
		this.byteValue = byteValue;
	}

	private static final Map<Byte, MaxAckType> BY_CODE_MAP = new LinkedHashMap<Byte, MaxAckType>();

	static {
		for (MaxAckType rae : MaxAckType.values()) {
			BY_CODE_MAP.put(rae.byteValue, rae);
		}
	}


	public static MaxAckType forCode(Byte code) {
		return BY_CODE_MAP.get(code);
	}
}
