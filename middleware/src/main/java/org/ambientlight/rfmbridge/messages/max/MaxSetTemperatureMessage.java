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

import org.ambientlight.rfmbridge.RequestMessage;
import org.ambientlight.room.entities.climate.util.MaxThermostateMode;
import org.ambientlight.room.entities.climate.util.MaxUtil;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxSetTemperatureMessage extends MaxMessage implements RequestMessage {

	public MaxSetTemperatureMessage() {
		payload = new byte[14];
		setMessageType(MaxMessageType.SET_TEMPERATURE);
		setFlags(FLAG_BROADCAST | FLAG_REQUEST_BURST);
	}


	@Override
	public byte[] getPayload() {
		if (getMode() != MaxThermostateMode.TEMPORARY)
			return Arrays.copyOf(payload, 11);
		else
			return payload;
	}


	public float getTemp() {
		return (payload[10] & 0x3f) / 2.0f;
	}


	public void setTemp(float temp) {
		// if (getMode() == MaxThermostateMode.AUTO) {
		// setTempWithoutChecks(temp);
		// return;
		// }

		if (temp > MaxUtil.MAX_TEMPERATURE) {
			temp = MaxUtil.MAX_TEMPERATURE;
		}
		if (temp < MaxUtil.MIN_TEMPERATURE) {
			temp = MaxUtil.MIN_TEMPERATURE;
		}
		setTempWithoutChecks(temp);
	}


	private void setTempWithoutChecks(float temp) {
		int amount = (int) (temp / 0.5f);
		payload[10] = (byte) (amount | payload[10]);
	}


	public MaxThermostateMode getMode() {
		return MaxThermostateMode.forCode(((payload[10] >> 6) & 0x3));
	}


	public void setMode(MaxThermostateMode mode) {
		int value = mode.byteValue << 6;
		payload[10] = (byte) (value | payload[10]);
		// if (mode == MaxThermostateMode.AUTO) {
		// setTempWithoutChecks(0.0f);
		// }
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


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.AckRequestMessage#getRetryTimeSec()
	 */
	@Override
	public int getTimeOutSec() {
		return 10;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.AckRequestMessage#getRetryCount()
	 */
	@Override
	public int getRetryCount() {
		return 10;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.AckRequestMessage#getCorrelation()
	 */
	@Override
	public String getCorrelation() {
		if (getSequenceNumber() == null)
			return null;
		return getSequenceNumber().toString();
	}
}
