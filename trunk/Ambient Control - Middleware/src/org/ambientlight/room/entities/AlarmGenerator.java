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

package org.ambientlight.room.entities;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.ambientlight.config.process.events.AlarmEvent;


/**
 * @author Florian Bornkessel
 * 
 */
public class AlarmGenerator extends EventGenerator {

	/**
	 * @param eventListener
	 * @param triggerConfig
	 */
	public void createAlarm(final AlarmEvent triggerConfig) {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
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
