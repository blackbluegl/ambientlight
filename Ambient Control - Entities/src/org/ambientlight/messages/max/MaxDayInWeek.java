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

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author Florian Bornkessel
 * 
 */
public enum MaxDayInWeek {
	MONDAY((byte) 0x02, Calendar.MONDAY), TUESDAY((byte) 0x03, Calendar.TUESDAY), WEDNESDAY((byte) 0x04, Calendar.WEDNESDAY), THURSDAY(
			(byte) 0x05, Calendar.THURSDAY), FRIDAY((byte) 0x6, Calendar.FRIDAY), SATURDAY((byte) 0x0, Calendar.SATURDAY), SUNDAY(
					(byte) 0x1, Calendar.SUNDAY);

	public final byte byteValue;
	public final int calendarDayInWeek;


	MaxDayInWeek(byte byteValue, int calendarDayInWeek) {
		this.byteValue = byteValue;
		this.calendarDayInWeek = calendarDayInWeek;
	}

	private static final Map<Byte, MaxDayInWeek> byteCodeMap = new LinkedHashMap<Byte, MaxDayInWeek>();
	private static final Map<Integer, MaxDayInWeek> calendarCodeMap = new LinkedHashMap<Integer, MaxDayInWeek>();

	static {
		for (MaxDayInWeek dayInWeekEnum : MaxDayInWeek.values()) {
			byteCodeMap.put(dayInWeekEnum.byteValue, dayInWeekEnum);
			calendarCodeMap.put(dayInWeekEnum.calendarDayInWeek, dayInWeekEnum);
		}
	}


	public static MaxDayInWeek forCalendarDayInWeek(int dayInWeek) {
		return calendarCodeMap.get(dayInWeek);
	}


	public static MaxDayInWeek forCode(int code) {
		return byteCodeMap.get(code);
	}
}
