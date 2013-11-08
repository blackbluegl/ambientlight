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
public enum MaxMessageType {
	PAIR_PING((byte) 0x00), PAIR_PONG((byte) 0x01), ACK((byte) 0x02), TIME_INFORMATION((byte) 0x03), CONFIG_WEEK_PROFILE(
			(byte) 0x10), CONFIG_TEMPERATURES((byte) 0x11), CONFIG_VALVE((byte) 0x12), ADD_LINK_PARTNER((byte) 0x20), REMOVE_LINK_PARTNER(
					(byte) 0x21), SET_GROUP_ID((byte) 0x22), REMOVE_GROUP_ID((byte) 0x23), SHUTTER_CONTACT_STATE((byte) 0x30), SET_TEMPERATURE(
							(byte) 0x40), WALL_THERMOSTAT_STATE((byte) 0x42), SET_COMFORT_TEMPERATURE((byte) 0x43), SET_ECO_TEMPERATURE(
									(byte) 0x44), PUSH_BUTTON_STATE((byte) 0x50), THERMOSTAT_STATE((byte) 0x60), SET_DISPLAY_ACTUAL_TEMPERATURE(
											(byte) 0x82), WAKE_UP((byte) 0xF1), RESET((byte) 0xF0), UNKNOWN((byte) 255);

	public final byte byteValue;


	MaxMessageType(byte byteValue) {
		this.byteValue = byteValue;
	}

	private static final Map<Byte, MaxMessageType> BY_CODE_MAP = new LinkedHashMap<Byte, MaxMessageType>();

	static {
		for (MaxMessageType rae : MaxMessageType.values()) {
			BY_CODE_MAP.put(rae.byteValue, rae);
		}
	}


	public static MaxMessageType forCode(byte code) {
		return BY_CODE_MAP.get(code);
	}
}
