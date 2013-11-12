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

package test;

import java.util.Calendar;
import java.util.Date;

import org.ambientlight.messages.max.MaxConfigureWeekProgrammMessage;
import org.ambientlight.messages.max.MaxDayInWeek;
import org.ambientlight.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.messages.max.MaxThermostateMode;
import org.ambientlight.messages.max.MaxTimeInformationMessage;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxMessageTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MaxSetTemperatureMessage temp = new MaxSetTemperatureMessage();
		temp.setMode(MaxThermostateMode.TEMPORARY);
		temp.setTemp(32f);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2012);
		cal.set(Calendar.MONTH, 11);
		cal.set(Calendar.DAY_OF_MONTH, 21);
		cal.set(Calendar.HOUR_OF_DAY, 19);
		cal.set(Calendar.MINUTE, 30);
		temp.setTemporaryUntil(cal.getTime());

		System.out.println(temp);

		MaxTimeInformationMessage time = new MaxTimeInformationMessage();
		Date now = new Date(System.currentTimeMillis());
		time.setTime(now);
		System.out.println(time);

		MaxConfigureWeekProgrammMessage week = new MaxConfigureWeekProgrammMessage();
		week.setSecondPart(false);
		week.setDay(MaxDayInWeek.SUNDAY);
		week.addEntry(week.new DayEntry(5, 29, 13.5f));
		week.addEntry(week.new DayEntry(21, 29, 23.5f));

		MaxConfigureWeekProgrammMessage week2 = new MaxConfigureWeekProgrammMessage();
		week2.setPayload(week.getPayload());
		System.out.println(week2.toString());
	}
}
