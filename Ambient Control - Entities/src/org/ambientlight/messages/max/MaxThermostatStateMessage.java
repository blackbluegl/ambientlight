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

import java.util.Date;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxThermostatStateMessage extends MaxMessage {

	public static final int MAX_VALVE_POSITION = 64;


	public MaxThermostatStateMessage() {
		payload = new byte[16];
		setMessageType(MaxMessageType.THERMOSTAT_STATE);
	}



	public MaxThermostateMode getMode() {
		return MaxThermostateMode.forCode(payload[10] & 0x3);
	}


	public boolean getDST() {
		return (payload[10] >> 2 & 0x1) > 0 ? true : false;
	}


	public boolean isLocked() {
		return (payload[10] >> 5 & 0x1) > 0 ? true : false;
	}

	public boolean hadRfError() {
		return (payload[10] >> 6 & 0x1) > 0 ? true : false;
	}

	public boolean isBatteryLow() {
		return (payload[10] >> 7) > 0 ? true : false;
	}


	public int getValvePosition() {
		return payload[11] & 0xff;
	}


	public float getSetTemp() {
		return (payload[12] & 0xff) / 2;
	}


	/**
	 * the actual temperature will be returned if mode is not TEMPORARY. Because
	 * the bytes will be used elsewhere for the UntilTime.
	 * 
	 * @return
	 */
	public Float getActualTemp() {
		if (this.getMode() == MaxThermostateMode.TEMPORARY)
			return null;
		int tempRaw = ((payload[13] & 0x1) << 8) + payload[14] & 0xff;
		return tempRaw / 10.0f;
	}


	public Date getTemporaryUntil() {
		if (this.getMode() != MaxThermostateMode.TEMPORARY)
			return null;
		byte[] time = new byte[] { payload[13], payload[14], payload[15] };
		return MaxUtil.parseUntilTime(time);
	}


	@Override
	public String toString() {
		String parent = super.toString() + "\n";
		String myString = "Mode: " + getMode() + "\n";
		myString += "Until: " + getTemporaryUntil() + "\n";
		myString += "DST: " + getDST() + "\n";
		myString += "Locked: " + isLocked() + "\n";
		myString += "BatteryLow: " + hadRfError() + "\n";
		myString += "RF-Error: " + isBatteryLow() + "\n";
		myString += "ValvePos: " + getValvePosition() + "\n";
		myString += "Set Temp: " + getSetTemp() + "\n";
		myString += "Act Temp: " + getActualTemp() + "\n";
		return parent + myString;
	}
}