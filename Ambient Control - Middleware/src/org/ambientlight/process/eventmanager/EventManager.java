package org.ambientlight.process.eventmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.events.AlarmEventConfiguration;
import org.ambientlight.process.events.EventConfiguration;
import org.ambientlight.room.entities.AlarmGenerator;


public class EventManager implements IEventManager, IEventManagerClient {

	Map<EventConfiguration, List<IEventListener>> eventMap = new HashMap<EventConfiguration, List<IEventListener>>();


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.events.IEventManager#register(org.ambientlight
	 * .process.events.IEventListener,
	 * org.ambientlight.process.trigger.EventTriggerConfiguration)
	 */
	@Override
	public void register(final IEventListener eventListener, final EventConfiguration triggerConfig) {
		System.out.println("EventManager: registering event:" + triggerConfig.toString());
		if (triggerConfig instanceof AlarmEventConfiguration) {
			AlarmEventConfiguration alarmConfig = (AlarmEventConfiguration) triggerConfig;
			((AlarmGenerator) AmbientControlMW.getRoom().eventGenerators.get(alarmConfig.eventGeneratorName))
			.createAlarm((AlarmEventConfiguration) triggerConfig);
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
	public void onEvent(EventConfiguration event) {

		List<IEventListener> eventListeners = this.eventMap.get(event);
		if (eventListeners != null) {
			for (IEventListener currentListener : eventListeners) {
				System.out.println("EventManager: onEvent called: " + event.toString());
				currentListener.handleEvent(event);
			}
		}
	}
}
