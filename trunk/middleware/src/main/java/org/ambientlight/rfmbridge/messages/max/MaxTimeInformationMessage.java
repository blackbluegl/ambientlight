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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.ambientlight.rfmbridge.RequestMessage;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxTimeInformationMessage extends MaxMessage implements RequestMessage {

	public MaxTimeInformationMessage() {
		payload = new byte[15];
		setMessageType(MaxMessageType.TIME_INFORMATION);
		setFlags(FLAG_REQUEST | FLAG_REQUEST_BURST);
	}


	public boolean isRequest() {
		if (payload.length < 11)
			return true;
		else
			return false;
	}


	public void setTime(Date time) {
		if (isRequest())
			return;
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(time);
		payload[10] = (byte) (calendar.get(Calendar.YEAR) - 2000);
		payload[11] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
		payload[12] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
		payload[13] = (byte) (calendar.get(Calendar.MINUTE) | (((calendar.get(Calendar.MONTH) + 1) & 0x0C) << 4));
		payload[14] = (byte) (calendar.get(Calendar.SECOND) | (((calendar.get(Calendar.MONTH) + 1) & 0x03) << 6));
		// use hardcoded value for now..
		this.setSummerTime(false);
	}


	public Date getTime() {
		if (isRequest())
			return null;
		int year = (payload[10] & 0xFF) + 2000;
		int day = payload[11] & 0xFF;
		int hour = payload[12] & 0x1F;
		int minute = payload[13] & 0x3F;
		int second = payload[14] & 0x3F;
		int month0Based = ((((payload[13] & 0xC0) >> 4) | ((payload[14] & 0xC0) >> 6)) & 0xFF) - 1;
		GregorianCalendar calendar = new GregorianCalendar(year, month0Based, day, hour, minute, second);
		return calendar.getTime();
	}


	public void setSummerTime(boolean summerTime) {
		payload[12] = (byte) (payload[12] & 0xDF);
		if (summerTime) {
			payload[12] = (byte) (payload[12] | 0x80);
		} else {
			payload[12] = (byte) (payload[12] | 0x40);
		}
	}


	public Boolean isSummerTime() {
		if (((payload[12] >> 6) & 0x01) == 1)
			return false;

		if (((payload[12] >> 7) & 0x01) == 1)
			return true;
		return null;
	}


	@Override
	public String toString() {
		String parent = super.toString();
		String result = "\nTime: " + String.valueOf(getTime()) + "\nisRequest: " + isRequest() + "\nisSummerTimer: "
				+ isSummerTime();
		return (parent + result);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.rfmbridge.RequestMessage#getTimeOutSec()
	 */
	@Override
	public int getTimeOutSec() {
		return 10;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.rfmbridge.RequestMessage#getRetryCount()
	 */
	@Override
	public int getRetryCount() {
		return 10;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.rfmbridge.RequestMessage#getCorrelation()
	 */
	@Override
	public String getCorrelation() {
		if (getSequenceNumber() == null)
			return null;
		return getSequenceNumber().toString();
	}
}