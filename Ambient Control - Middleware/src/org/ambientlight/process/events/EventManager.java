package org.ambientlight.process.events;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.ambientlight.process.trigger.AlarmEventConfiguration;
import org.ambientlight.process.trigger.EventTriggerConfiguration;
import org.ambientlight.process.trigger.SceneryEntryEventConfiguration;
import org.ambientlight.scenery.actor.switching.SwitchingConfiguration;


public class EventManager implements IEventManager, IEventManagerClient {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.events.IEventManager#register(org.ambientlight
	 * .process.events.IEventListener,
	 * org.ambientlight.process.trigger.EventTriggerConfiguration)
	 */
	@Override
	public void register(final IEventListener eventListener, final EventTriggerConfiguration triggerConfig) {

		if(triggerConfig instanceof AlarmEventConfiguration){
			TimerTask task = new TimerTask() {

				@Override
				public void run() {
					eventListener.handleEvent(triggerConfig);

				}
			};

			Calendar now = Calendar.getInstance();
			Calendar alarm = Calendar.getInstance();
			alarm.set(Calendar.HOUR_OF_DAY, ((AlarmEventConfiguration) triggerConfig).hour);
			alarm.set(Calendar.MINUTE, ((AlarmEventConfiguration) triggerConfig).minute);
			if (alarm.before(now)) {
				alarm.add(Calendar.DAY_OF_MONTH, 1);
			}

			Timer timer = new Timer();
			timer.schedule(task, alarm.getTime());
		}



	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.events.IEventManagerClient#onSceneryChange(org
	 * .ambientlight.process.trigger.SceneryEntryEventConfiguration)
	 */
	@Override
	public void onSceneryChange(SceneryEntryEventConfiguration sceneryEntry) {

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.events.IEventManagerClient#onAlarm(org.ambientlight
	 * .process.trigger.AlarmEventConfiguration)
	 */
	@Override
	public void onAlarm(AlarmEventConfiguration alarm) {

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.events.IEventManagerClient#onSwitchChange(org
	 * .ambientlight.scenery.actor.switching.SwitchingConfiguration, boolean)
	 */
	@Override
	public void onSwitchChange(SwitchingConfiguration config, boolean powerState) {

	}
}
