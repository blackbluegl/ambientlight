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

import org.ambientlight.messages.max.MaxConfigValveMessage;
import org.ambientlight.messages.max.MaxConfigureTemperaturesMessage;
import org.ambientlight.messages.max.MaxConfigureWeekProgrammMessage;
import org.ambientlight.messages.max.MaxDayInWeek;
import org.ambientlight.messages.max.MaxPairPingMessage;
import org.ambientlight.messages.max.MaxPairPongMessage;
import org.ambientlight.messages.max.MaxRemoveGroupIdMessage;
import org.ambientlight.messages.max.MaxSetGroupIdMessage;
import org.ambientlight.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.messages.max.MaxShutterContactStateMessage;
import org.ambientlight.messages.max.MaxThermostateMode;
import org.ambientlight.messages.max.MaxTimeInformationMessage;
import org.ambientlight.messages.max.MaxWakeUpMessage;


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

		MaxConfigValveMessage valve = new MaxConfigValveMessage();
		valve.setBoostDuration(23);
		valve.setBoostValvePosition(100);
		MaxConfigValveMessage.DecalcEntry decalc = valve.new DecalcEntry();
		decalc.day = MaxDayInWeek.FRIDAY;
		decalc.hour = 23;
		valve.setDecalc(decalc);
		valve.setMaxValvePosition(300);
		valve.setValveOffset(256);

		System.out.println(valve);

		MaxConfigureTemperaturesMessage temps = new MaxConfigureTemperaturesMessage();
		temps.setComfortTemp(44);
		temps.setEcoTemp(0);
		temps.setMaxTemp(2000);
		temps.setMinTemp(-22);
		temps.setOffsetTemp(44);
		temps.setWindowOpenTemp(-2);
		temps.setWindowOpenTime(100);
		System.out.println(temps);

		MaxPairPingMessage ping = new MaxPairPingMessage();
		ping.setToAdress(2879);
		ping.getPayload()[10] = 0x16;
		ping.getPayload()[11] = 0x01;
		ping.getPayload()[12] = (byte) 0xFF;
		ping.getPayload()[13] = 0x4A;
		ping.getPayload()[14] = 0x48;
		ping.getPayload()[15] = 0x41;
		ping.getPayload()[16] = 0x30;
		ping.getPayload()[17] = 0x30;
		ping.getPayload()[18] = 0x30;
		ping.getPayload()[19] = 0x38;
		ping.getPayload()[20] = 0x32;
		ping.getPayload()[21] = 0x39;
		ping.getPayload()[22] = 0x32;
		System.out.println(ping);

		MaxPairPongMessage pong = new MaxPairPongMessage();
		System.out.println(pong);

		MaxSetGroupIdMessage setGroup = new MaxSetGroupIdMessage();
		setGroup.setGroupId(201);
		System.out.println(setGroup);

		MaxRemoveGroupIdMessage removeGroup = new MaxRemoveGroupIdMessage();
		System.out.println(removeGroup);

		MaxShutterContactStateMessage shutter = new MaxShutterContactStateMessage();
		shutter.getPayload()[10] = (byte) 0xC1;
		System.out.println(shutter);

		System.out.println(new MaxWakeUpMessage());
	}
}
