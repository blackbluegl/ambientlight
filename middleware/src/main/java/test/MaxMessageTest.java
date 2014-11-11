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
import java.util.GregorianCalendar;

import org.ambientlight.rfmbridge.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.room.entities.climate.util.MaxThermostateMode;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxMessageTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// MaxSetTemperatureMessage temp = new MaxSetTemperatureMessage();
		// temp.setMode(MaxThermostateMode.TEMPORARY);
		// temp.setTemp(11.5f);
		//
		// Calendar cal = Calendar.getInstance();
		// cal.set(Calendar.YEAR, 2012);
		// cal.set(Calendar.MONTH, 11);
		// cal.set(Calendar.DAY_OF_MONTH, 21);
		// cal.set(Calendar.HOUR_OF_DAY, 19);
		// cal.set(Calendar.MINUTE, 30);
		// temp.setTemporaryUntil(cal.getTime());
		//
		// System.out.println(temp);
		//
		// MaxTimeInformationMessage time = new MaxTimeInformationMessage();
		// Date now = new Date(System.currentTimeMillis());
		// time.setTime(now);
		// System.out.println(time);
		//
		// MaxConfigureWeekProgrammMessage week = new MaxConfigureWeekProgrammMessage();
		// week.setSecondPart(false);
		// week.setDay(MaxDayInWeek.SUNDAY);
		// week.addEntry(new DayEntry(5, 29, 13.5f));
		// week.addEntry(new DayEntry(21, 29, 23.5f));
		// System.out.println(week);
		// MaxConfigureWeekProgrammMessage week2 = new MaxConfigureWeekProgrammMessage();
		// week2.setPayload(week.getPayload());
		// System.out.println(week2);
		// week2.setPayload(new byte[] { (byte) 0xB3, 0x01, 0x10, 0x02, (byte) 0x8F, (byte) 0xC2, 0x08, 0x69, (byte) 0x91, 0x00,
		// 0x02, 0x44, 0x4F, 0x54, 0x6C, 0x44, (byte) 0xD3, 0x59, 0x11, 0x4D, 0x20, 0x4D, 0x20, 0x4D, 0x20 });
		// System.out.println(week2);
		// MaxConfigureWeekProgrammMessage week2 = new
		// MaxConfigureWeekProgrammMessage();
		// week2.setPayload(week.getPayload());
		// System.out.println(week2.toString());
		//

		// MaxConfigValveMessage valve = new MaxConfigValveMessage();
		// valve.setBoostDuration(23);
		// valve.setBoostValvePosition(100);
		// MaxConfigValveMessage.DecalcEntry decalc = valve.new DecalcEntry();
		// decalc.day = MaxDayInWeek.FRIDAY;
		// decalc.hour = 23;
		// valve.setDecalc(decalc);
		// valve.setMaxValvePosition(30);
		// valve.setValveOffset(25);
		//
		// System.out.println(valve);
		//
		// MaxConfigureTemperaturesMessage temps = new
		// MaxConfigureTemperaturesMessage();
		// temps.setComfortTemp(44);
		// temps.setEcoTemp(0);
		// temps.setMaxTemp(2000);
		// temps.setMinTemp(-22);
		// temps.setOffsetTemp(44);
		// temps.setWindowOpenTemp(-2);
		// temps.setWindowOpenTime(100);
		// System.out.println(temps);
		//
		// MaxPairPingMessage ping = new MaxPairPingMessage();
		// ping.setToAdress(2879);
		// ping.getPayload()[10] = 0x16;
		// ping.getPayload()[11] = 0x01;
		// ping.getPayload()[12] = (byte) 0xFF;
		// ping.getPayload()[13] = 0x4A;
		// ping.getPayload()[14] = 0x48;
		// ping.getPayload()[15] = 0x41;
		// ping.getPayload()[16] = 0x30;
		// ping.getPayload()[17] = 0x30;
		// ping.getPayload()[18] = 0x30;
		// ping.getPayload()[19] = 0x38;
		// ping.getPayload()[20] = 0x32;
		// ping.getPayload()[21] = 0x39;
		// ping.getPayload()[22] = 0x32;
		// System.out.println(ping);
		//
		// MaxPairPongMessage pong = new MaxPairPongMessage();
		// System.out.println(pong);
		//
		// MaxSetGroupIdMessage setGroup = new MaxSetGroupIdMessage();
		// setGroup.setGroupId(201);
		// System.out.println(setGroup);
		//
		// MaxRemoveGroupIdMessage removeGroup = new MaxRemoveGroupIdMessage();
		// System.out.println(removeGroup);
		//
		// MaxShutterContactStateMessage shutter = new
		// MaxShutterContactStateMessage();
		// shutter.getPayload()[10] = (byte) 0xC1;
		// System.out.println(shutter);
		//
		// System.out.println(new MaxWakeUpMessage());

		// System.out.println("timeTest");
		// MaxTimeInformationMessage timeTest = new MaxTimeInformationMessage();
		// timeTest.setPayload(new byte[] { (byte) 0x2D, (byte) 0x05, (byte) 0x03, (byte) 0x02, (byte) 0x8F, (byte) 0xC2,
		// (byte) 0x08, (byte) 0x76, (byte) 0xF5, (byte) 0x00, (byte) 0x0E, (byte) 0x15, (byte) 0x8E, (byte) 0x99,
		// (byte) 0xB8
		//
		// });
		// System.out.println(timeTest);
		// timeTest.setTime(timeTest.getTime());

		// Calendar cal = Calendar.getInstance();
		// cal.set(Calendar.YEAR, 2012);
		// cal.set(Calendar.MONTH, 0);
		// cal.set(Calendar.DAY_OF_MONTH, 21);
		// cal.set(Calendar.HOUR_OF_DAY, 19);
		// cal.set(Calendar.MINUTE, 30);
		// timeTest.setTime(cal.getTime());
		// System.out.println(timeTest);
		// timeTest.setPayload(timeTest.getPayload());
		// System.out.println(timeTest);
		// MaxThermostatStateMessage thermostate = new MaxThermostatStateMessage();
		// thermostate.setPayload(new byte[] { 0x02, 0x04, 0x60, 0x08, 0x69, (byte) 0x91, 0x00, 0x00, 0x00, 0x00, 0x18, 0x0D,
		// 0x2C,
		// 0x01, 0x03 });
		// System.out.println(thermostate);
		//
		// MaxTimeInformationMessage time = new MaxTimeInformationMessage();
		// time.setSequenceNumber(131);
		// time.setFromAdress(167874);
		// time.setToAdress(537069);
		// Calendar cal = new GregorianCalendar().getInstance();
		// cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		// cal.set(Calendar.MONTH, Calendar.NOVEMBER);
		// cal.set(Calendar.DAY_OF_MONTH, 10);
		// cal.set(Calendar.HOUR_OF_DAY, 23);
		// cal.set(Calendar.MINUTE, 56);
		// cal.set(Calendar.SECOND, 55);
		// time.setTime(cal.getTime());
		// System.out.println(time);

		// ClimateManager - handleMessage(): handle MaxMessage: TIME_INFORMATION - SeqNr: 131 from: 167874 to: 537069 with
		// groupId: 0 Flags: 0x5
		// Payload: 0x83 0x05 0x03 0x02 0x8F 0xC2 0x08 0x31 0xED 0x00 0x0E 0x0A 0x47 0xB8 0xF7
		// Payload: 0x83 0x05 0x03 0x02 0x8F 0xC2 0x08 0x31 0xED 0x00 0x0E 0x0A 0x07 0xB8 0xF7
		// Payload: 0x83 0x05 0x03 0x02 0x8F 0xC2 0x08 0x31 0xED 0x00 0x0E 0x0A 0x47 0xB8 0xF7
		// Time: Mon Nov 10 07:56:55 CET 2014
		// isRequest: false

		MaxSetTemperatureMessage temp = new MaxSetTemperatureMessage();
		temp.setFromAdress(41);
		temp.setToAdress(551313);
		temp.setTemp(4.5f);
		temp.setMode(MaxThermostateMode.TEMPORARY);
		Calendar newTime = GregorianCalendar.getInstance();
		newTime.add(Calendar.MINUTE, 30);
		temp.setTemporaryUntil(newTime.getTime());
		System.out.println(temp);
		temp.setPayload(temp.getPayload());
		System.out.println(temp);

	}
}
