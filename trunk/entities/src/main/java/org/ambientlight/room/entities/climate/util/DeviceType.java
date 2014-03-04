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

package org.ambientlight.room.entities.climate.util;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author Florian Bornkessel
 * 
 */
public enum DeviceType {

	CUBE((byte) 0x00), HEATING_THERMOSTAT((byte) 0x01), HEATING_THERMOSTAT_PLUS((byte) 0x02), WALL_MOUNTED_THERMOSTAT((byte) 0x03), SHUTTER_CONTACT(
			(byte) 0x04), PUSH_BUTTON((byte) 0x5);

	private static final Map<Byte, DeviceType> MAPPING = new LinkedHashMap<Byte, DeviceType>();

	static {
		for (DeviceType rae : DeviceType.values()) {
			MAPPING.put(rae.byteValue, rae);
		}
	}

	public final byte byteValue;


	DeviceType(byte byteValue) {
		this.byteValue = byteValue;
	}


	public static DeviceType forCode(byte code) {
		return MAPPING.get(code);
	}
}
