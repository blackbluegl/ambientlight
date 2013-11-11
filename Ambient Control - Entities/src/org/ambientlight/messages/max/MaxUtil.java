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
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxUtil {

	public static Date parseUntilTime(byte[] untilTime) {
		if (untilTime.length < 3)
			return null;

		int year = 2000 + (untilTime[1] & 0x3f);
		int month = ((untilTime[0] & 0xE0) >> 4) | (untilTime[1] & 0xFF >> 7);
		int day = untilTime[0] & 0x1f;
		int timeInMinutes = (untilTime[2] & 0x3f) * 30;
		int hours = (int) TimeUnit.MINUTES.toHours(timeInMinutes);
		int minutes = (int) TimeUnit.MINUTES.toMinutes(timeInMinutes) - (int) TimeUnit.HOURS.toMinutes(hours);

		Calendar calendar = GregorianCalendar.getInstance();
		calendar.set(year, month - 1, day, hours, minutes, 00);
		return calendar.getTime();
	}


	public static byte[] getUntilTime(Date until) {
		byte[] result = new byte[3];
		GregorianCalendar untilCalendar = new GregorianCalendar(Locale.GERMANY);
		untilCalendar.setTime(until);

		int day = untilCalendar.get(Calendar.DAY_OF_MONTH);
		result[0] = (byte) (day);

		int year = untilCalendar.get(Calendar.YEAR) - 2000;
		result[1] = (byte) (year & 0xff);

		int month = untilCalendar.get(Calendar.MONTH) + 1;
		result[0] = (byte) (month << 4 & 0xE0 | result[0]);
		result[1] = (byte) ((month << 7) | result[1]);

		int halfHourFromMinute = untilCalendar.get(Calendar.MINUTE) / 30;
		int halfHours = untilCalendar.get(Calendar.HOUR_OF_DAY) * 2;
		int timeAmount = halfHourFromMinute + halfHours;
		result[2] = (byte) timeAmount;

		return result;
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


	public static String getHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += "0x" + String.format("%02X", b[i] & 0xFF) + " ";
		}
		return result;
	}

}
