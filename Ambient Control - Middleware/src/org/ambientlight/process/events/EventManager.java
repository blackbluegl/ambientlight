package org.ambientlight.process.events;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.trigger.AlarmEventConfiguration;
import org.ambientlight.process.trigger.EventTriggerConfiguration;


public class EventManager implements IEventManager, IEventManagerClient {

	Map<EventTriggerConfiguration, IEventListener> eventMap = new HashMap<EventTriggerConfiguration, IEventListener>();


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

		if (triggerConfig instanceof AlarmEventConfiguration) {
			AmbientControlMW.getRoom().alarmManager.createAlarm(this, (AlarmEventConfiguration) triggerConfig);
			eventMap.put(triggerConfig, eventListener);
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.events.IEventManagerClient#onEvent(org.ambientlight
	 * .process.trigger.EventTriggerConfiguration, java.lang.String)
	 */
	@Override
	public void onEvent(EventTriggerConfiguration event, String eventGeneratorName) {
		IEventListener eventListener = this.eventMap.get(event);
		eventListener.handleEvent(event, eventGeneratorName);

	}
}
