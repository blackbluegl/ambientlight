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


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxSetTemperatureMessage extends MaxMessage {

	public static final float MAX_TEMPERATUR = 31.5f;
	public static final float MIN_TEMPERATUR = 4.5f;


	public MaxSetTemperatureMessage() {
		payload = new byte[14];
		setMessageType(MaxMessageType.SET_TEMPERATURE);
		setFlags(FLAG_REQUEST | FLAG_0X1);
	}


	@Override
	public byte[] getPayload() {
		if (getMode() != MaxThermostateMode.TEMPORARY)
			return Arrays.copyOf(payload, 11);
		else
			return payload;
	}


	public float getTemp() {
		return (float) (payload[10] & 0x3f) / 2;
	}


	public void setTemp(float temp) {
		if (temp > MAX_TEMPERATUR) {
			temp = MAX_TEMPERATUR;
		}
		if (temp < MIN_TEMPERATUR) {
			temp = MIN_TEMPERATUR;
		}
		int amount = (int) (temp / 0.5f);
		payload[10] = (byte) (amount | payload[10]);
	}


	public MaxThermostateMode getMode() {
		return MaxThermostateMode.forCode(((payload[10] >> 6) & 0x3));
	}


	public void setMode(MaxThermostateMode mode) {
		int value = mode.byteValue << 6;
		payload[10] = (byte) (value | payload[10]);
	}


	public Date getTemporaryUntil() {
		if (this.getMode() != MaxThermostateMode.TEMPORARY)
			return null;
		byte[] time = new byte[] { payload[11], payload[12], payload[13] };
		return MaxUtil.parseUntilTime(time);
	}


	public void setTemporaryUntil(Date until) {
		if (this.getMode() != MaxThermostateMode.TEMPORARY)
			return;
		byte[] result = MaxUtil.getUntilTime(until);
		payload[11] = result[0];
		payload[12] = result[1];
		payload[13] = result[2];
	}


	@Override
	public String toString() {
		String parent = super.toString() + "\n";
		String result = "Temperature: " + getTemp() + "\n";
		result += "Mode: " + getMode() + "\n";
		result += "Until: " + getTemporaryUntil();
		return parent + result;
	}
}
