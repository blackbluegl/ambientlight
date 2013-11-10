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

		GregorianCalendar calendar = new GregorianCalendar(Locale.GERMANY);
		calendar.set(year, month - 1, day, hours, minutes, 00);
		return calendar.getTime();
	}
}
