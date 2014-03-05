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

import org.ambientlight.Manager;
import org.ambientlight.Persistence;
import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.room.entities.alarm.AlarmManagerConfiguration;
import org.ambientlight.events.DailyAlarmEvent;
import org.ambientlight.events.EventManager;
import org.ambientlight.room.entities.FeatureFacade;
import org.ambientlight.room.entities.SwitchablesHandler;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.actor.types.SwitchType;
import org.ambientlight.room.entities.features.actor.types.SwitchableId;


/**
 * @author Florian Bornkessel
 * 
 */
public class AlarmManager extends Manager implements SwitchablesHandler {

	private AlarmManagerConfiguration config;

	private CallBackManager callBackManager;

	private EventManager eventManager;

	private Timer timer = new Timer();

	private Map<String, TimerTask> alarmTasks = new HashMap<String, TimerTask>();


	public AlarmManager(AlarmManagerConfiguration config, EventManager eventManager, CallBackManager callBackMananger,
			FeatureFacade entitiesFacade, Persistence persistence) {
		super();
		this.config = config;
		this.eventManager = eventManager;
		this.callBackManager = callBackMananger;
		this.persistence = persistence;

		for (Entry<String, DailyAlarm> current : config.alarms.entrySet()) {
			createAlarmEvent(current.getKey(), current.getValue());
			entitiesFacade.registerSwitchable(this, current.getValue(), SwitchType.ALARM);
		}
	}


	/**
	 * @param eventListener
	 * @param triggerConfig
	 */
	public void deleteAlarm(final String name) {

		persistence.beginTransaction();

		DailyAlarm alarm = this.config.alarms.get(name);

		removeAlarmEvent(name, alarm);

		this.config.alarms.remove(name);

		persistence.commitTransaction();

		callBackManager.roomConfigurationChanged();
	}


	/**
	 * @param eventListener
	 * @param triggerConfig
	 */
	public void createOrUpdateAlarm(final DailyAlarm alarm) {

		persistence.beginTransaction();

		this.config.alarms.put(alarm.getId(), alarm);

		if (alarm.getPowerState()) {
			createAlarmEvent(alarm.getId(), alarm);
		} else {
			removeAlarmEvent(alarm.getId(), alarm);
		}

		persistence.commitTransaction();

		callBackManager.roomConfigurationChanged();
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


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.room.entities.SwitchablesHandler#setPowerState(java.
	 * lang.String, org.ambientlight.room.entities.switches.SwitchType, boolean)
	 */
	@Override
	public void setPowerState(String id, SwitchType type, boolean powerState) {
		DailyAlarm alarm = config.alarms.get(id);

		if (alarm == null) {
			System.out.println("RemoteSwitchManager handleSwitchChange(): got request for unknown device: =" + id);
			return;
		}

		alarm.setPowerState(powerState);
		this.createOrUpdateAlarm(alarm);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.SwitchablesHandler#getSwitchable(org.
	 * ambientlight.room.entities.features.actor.types.SwitchableId)
	 */
	@Override
	public Switchable getSwitchable(SwitchableId id) {
		return config.alarms.get(id.id);
	}
}
