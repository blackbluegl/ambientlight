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
import java.util.Timer;
import java.util.TimerTask;

import org.ambientlight.config.events.DailyAlarmEvent;
import org.ambientlight.config.room.entities.alarm.AlarmManagerConfiguration;
import org.ambientlight.config.room.entities.alarm.DailyAlarm;
import org.ambientlight.eventmanager.EventManager;


/**
 * @author Florian Bornkessel
 * 
 */
public class AlarmManager {

	public AlarmManagerConfiguration config;

	public EventManager eventManager;


	/**
	 * @param eventListener
	 * @param triggerConfig
	 */
	public void createAlarm(final DailyAlarm alarm) {

		transaction einfügen

		TimerTask task = new TimerTask() {

			@Override
			public void run() {

				DailyAlarmEvent alarmEvent = new DailyAlarmEvent(alarm.hour, alarm.minute, sourceName)

				eventManager.onEvent(triggerConfig);
			}
		};

		Calendar now = Calendar.getInstance();
		Calendar alarm = Calendar.getInstance();
		alarm.set(Calendar.HOUR_OF_DAY, triggerConfig.hour);
		alarm.set(Calendar.MINUTE, triggerConfig.minute);
		if (alarm.before(now)) {
			alarm.add(Calendar.DAY_OF_MONTH, 1);
		}

		Timer timer = new Timer();
		timer.schedule(task, alarm.getTime());
	}
}
