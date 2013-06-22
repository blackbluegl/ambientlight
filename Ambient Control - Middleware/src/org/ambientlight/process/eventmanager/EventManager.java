package org.ambientlight.process.eventmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.events.Event;
import org.ambientlight.process.trigger.AlarmEventTriggerConfiguration;
import org.ambientlight.process.trigger.EventTriggerConfiguration;
import org.ambientlight.room.entities.AlarmGenerator;


public class EventManager implements IEventManager, IEventManagerClient {

	Map<EventTriggerConfiguration, List<IEventListener>> eventMap = new HashMap<EventTriggerConfiguration, List<IEventListener>>();


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

		if (triggerConfig instanceof AlarmEventTriggerConfiguration) {
			((AlarmGenerator) AmbientControlMW.getRoom().eventGenerators.get(triggerConfig.eventGeneratorName))
			.createAlarm((AlarmEventTriggerConfiguration) triggerConfig);
		}

		List<IEventListener> eventListenerList = eventMap.get(triggerConfig);
		if (eventListenerList == null) {
			eventListenerList = new ArrayList<IEventListener>();
			eventMap.put(triggerConfig, eventListenerList);
		}
		eventListenerList.add(eventListener);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.events.IEventManagerClient#onEvent(org.ambientlight
	 * .process.trigger.EventTriggerConfiguration, java.lang.String)
	 */
	@Override
	public void onEvent(Event event, EventTriggerConfiguration correlation) {

		List<IEventListener> eventListeners = this.eventMap.get(correlation);
		if (eventListeners != null) {
			for (IEventListener currentListener : eventListeners) {
				currentListener.handleEvent(event, correlation);
			}
		}
	}
}
