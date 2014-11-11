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
import java.util.HashMap;
import java.util.Map;

import org.ambientlight.config.messages.DispatcherConfiguration;
import org.ambientlight.config.messages.DispatcherType;
import org.ambientlight.config.room.entities.climate.DayEntry;
import org.ambientlight.config.room.entities.climate.MaxDayInWeek;
import org.ambientlight.rfmbridge.Dispatcher;
import org.ambientlight.rfmbridge.DispatcherManager;
import org.ambientlight.rfmbridge.QeueManager;
import org.ambientlight.rfmbridge.messages.max.MaxConfigureWeekProgrammMessage;
import org.ambientlight.rfmbridge.messages.max.MaxDispatcher;
import org.ambientlight.rfmbridge.messages.max.MaxMessage;
import org.ambientlight.rfmbridge.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.rfmbridge.messages.max.MaxTimeInformationMessage;
import org.ambientlight.rfmbridge.messages.max.MaxWakeUpMessage;
import org.ambientlight.room.entities.climate.util.MaxThermostateMode;


/**
 * @author Florian Bornkessel
 * 
 */
public class MessageQueueTest {

	public static void main(String[] args) throws InterruptedException {
		QeueManager manager = new QeueManager();
		MessageDump dump = new MessageDump();
		manager.registerMessageListener(DispatcherType.MAX, dump);

		DispatcherConfiguration config = new DispatcherConfiguration();
		config.hostName = "max-bridge";
		config.port = 30000;
		config.type = DispatcherType.MAX;

		MaxDispatcher dispatcher = new MaxDispatcher(config, manager);

		Map<DispatcherType, Dispatcher> dispatchers = new HashMap<DispatcherType, Dispatcher>();
		dispatchers.put(DispatcherType.MAX, dispatcher);
		// dispatchers.put(DispatcherType.SYSTEM, dispatcher);

		DispatcherManager df = new DispatcherManager(manager, dispatchers);
		manager.dispatcherManager = df;
		manager.startQeues();
		df.startDispatchers();

		//
		// MaxFactoryResetMessage reset = new MaxFactoryResetMessage();
		// reset.setFromAdress(41);
		// reset.setToAdress(adress);

		MaxWakeUpMessage wakeUp = new MaxWakeUpMessage();
		wakeUp.setFromAdress(41);
		wakeUp.setToAdress(431563);
		wakeUp.setSequenceNumber(11);
		wakeUp.setGroupNumber(4);
		wakeUp.setFlags(MaxMessage.FLAG_REQUEST_BURST | MaxMessage.FLAG_REQUEST);

		manager.putOutMessage(wakeUp);

		// createWeek(manager, MaxDayInWeek.MONDAY, 5f, 1);
		// createWeek(manager, MaxDayInWeek.TUESDAY, 12f, 2);
		// createWeek(manager, MaxDayInWeek.WEDNESDAY, 13f, 3);
		// createWeek(manager, MaxDayInWeek.THURSDAY, 14f, 4);
		// createWeek(manager, MaxDayInWeek.FRIDAY, 15f, 5);
		// createWeek(manager, MaxDayInWeek.SATURDAY, 16f, 6);
		// createWeek(manager, MaxDayInWeek.SUNDAY, 17f, 7);

		Calendar date = Calendar.getInstance();
		// date.add(Calendar.DAY_OF_WEEK, -1);
		MaxTimeInformationMessage time = new MaxTimeInformationMessage();
		time.setTime(date.getTime());
		time.setFromAdress(41);
		time.setToAdress(431563);
		time.setSequenceNumber(33);
		manager.putOutMessage(time);
		System.out.println(time);

		MaxSetTemperatureMessage temp = new MaxSetTemperatureMessage();
		temp.setFromAdress(41);
		temp.setToAdress(431563);
		temp.setTemp(4.5f);
		temp.setMode(MaxThermostateMode.TEMPORARY);
		Calendar newTime = GregorianCalendar.getInstance();
		newTime.add(Calendar.MINUTE, 30);
		newTime.add(Calendar.DAY_OF_WEEK, 0);
		temp.setTemporaryUntil(newTime.getTime());
		manager.putOutMessage(temp);
		System.out.println("finished");
	}


	/**
	 * @param manager
	 */
	private static void createWeek(QeueManager manager, MaxDayInWeek dayInWeek, float temp, int sequenceNumber) {
		MaxConfigureWeekProgrammMessage week = new MaxConfigureWeekProgrammMessage();
		week.setDay(dayInWeek);
		DayEntry entry = new DayEntry(24, 0, temp);
		week.addEntry(entry);
		week.setFromAdress(41);
		week.setSecondPart(false);
		week.setSequenceNumber(sequenceNumber);
		week.setToAdress(431563);
		// byte[] mod = week.getPayload();
		// mod[14] = (byte) 0xFF;
		// mod[13] = (byte) 0xFF;
		// week.setPayload(mod);
		week.setFlags(MaxMessage.FLAG_REQUEST_BURST);
		System.out.println(week);
		manager.putOutMessage(week);
	}
}
