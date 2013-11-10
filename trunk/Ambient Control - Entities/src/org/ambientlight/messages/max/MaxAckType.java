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

package org.ambientlight.messages.max;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author Florian Bornkessel
 * 
 */
public enum MaxAckType {
	THERMOSTATE(0x01), CUBE(0x01);

	public final int byteValue;


	MaxAckType(int byteValue) {
		this.byteValue = byteValue;
	}

	private static final Map<Integer, MaxAckType> BY_CODE_MAP = new LinkedHashMap<Integer, MaxAckType>();

	static {
		for (MaxAckType rae : MaxAckType.values()) {
			BY_CODE_MAP.put(rae.byteValue, rae);
		}
	}


	public static MaxAckType forCode(int code) {
		return BY_CODE_MAP.get(code);
	}
}
