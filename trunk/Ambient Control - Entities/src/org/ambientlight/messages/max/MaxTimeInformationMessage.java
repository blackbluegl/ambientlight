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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxTimeInformationMessage extends MaxMessage {

	public MaxTimeInformationMessage() {
		payload = new byte[15];
		setMessageType(MaxMessageType.TIME_INFORMATION);
		setFlags(FLAG_REQUEST | FLAG_0X1);
	}


	public void setTime(Date time) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(time);
		payload[10] = (byte) (calendar.get(Calendar.YEAR) - 2000);
		payload[11] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
		payload[12] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
		payload[13] = (byte) (calendar.get(Calendar.MINUTE) | (((calendar.get(Calendar.MONTH) + 1) & 0x0C) << 4));
		payload[14] = (byte) (calendar.get(Calendar.SECOND) | (((calendar.get(Calendar.MONTH) + 1) & 0x03) << 6));
	}


	public Date getTime() {
		int year = payload[10] + 2000;
		int day = payload[11];
		int hour = payload[12];
		int minute = payload[13] & 0x3F;
		int second = payload[14] & 0x3F;
		int month = (((payload[13] & 0xF0) >> 4) | ((payload[14] & 0xC0) >> 6)) - 1;
		GregorianCalendar calendar = new GregorianCalendar(year, month, day, hour, minute, second);
		return calendar.getTime();
	}


	@Override
	public String toString() {
		String parent = super.toString();
		String result = "\nTime: " + getTime().toString();
		return (parent + result);
	}
}