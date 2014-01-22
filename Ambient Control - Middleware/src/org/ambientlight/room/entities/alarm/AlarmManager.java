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

package org.ambientlight.room.entities.alarm;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.events.DailyAlarmEvent;
import org.ambientlight.config.room.entities.alarm.AlarmManagerConfiguration;
import org.ambientlight.config.room.entities.alarm.DailyAlarm;
import org.ambientlight.eventmanager.EventManager;
import org.ambientlight.room.Persistence;


/**
 * @author Florian Bornkessel
 * 
 */
public class AlarmManager {

	public AlarmManagerConfiguration config;

	public EventManager eventManager;

	private Timer timer = new Timer();

	private Map<String, TimerTask> alarmTasks = new HashMap<String, TimerTask>();


	public AlarmManager(AlarmManagerConfiguration config, EventManager eventManager) {
		super();
		this.config = config;
		this.eventManager = eventManager;

		for (Entry<String, DailyAlarm> current : config.alarms.entrySet()) {
			createAlarmEvent(current.getKey(), current.getValue());
		}
	}


	/**
	 * @param eventListener
	 * @param triggerConfig
	 */
	public void deleteAlarm(final String name) {

		Persistence.beginTransaction();

		DailyAlarm alarm = this.config.alarms.get(name);

		removeAlarmEvent(name, alarm);

		this.config.alarms.remove(name);

		Persistence.commitTransaction();

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
	}


	/**
	 * @param eventListener
	 * @param triggerConfig
	 */
	public void createOrUpdateAlarm(final String name, final DailyAlarm alarm) {

		Persistence.beginTransaction();

		this.config.alarms.put(name, alarm);

		if (alarm.getPowerState()) {
			createAlarmEvent(name, alarm);
		} else {
			removeAlarmEvent(name, alarm);
		}

		Persistence.commitTransaction();

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
	}


	private void removeAlarmEvent(String name, DailyAlarm alarm) {
		alarmTasks.get(name).cancel();
	}


	/**
	 * @param name
	 * @param alarm
	 */
	private void createAlarmEvent(final String name, final DailyAlarm alarm) {
		Calendar now = Calendar.getInstance();
		Calendar alarmCalendar = Calendar.getInstance();
		alarmCalendar.set(Calendar.HOUR_OF_DAY, alarm.hour);
		alarmCalendar.set(Calendar.MINUTE, alarm.minute);
		if (alarmCalendar.before(now)) {
			alarmCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		TimerTask task = new TimerTask() {

			@Override
			public void run() {

				DailyAlarmEvent alarmEvent = new DailyAlarmEvent(name, alarm.hour, alarm.minute);
				eventManager.onEvent(alarmEvent);
			}
		};

		timer.scheduleAtFixedRate(task, alarmCalendar.getTime(), 24 * 3600 * 1000);
		alarmTasks.put(name, task);
	}
}
