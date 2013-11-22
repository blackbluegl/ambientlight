package org.ambientlight.process.eventmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.process.events.AlarmEvent;
import org.ambientlight.config.process.events.Event;
import org.ambientlight.room.entities.AlarmGenerator;


public class EventManager implements IEventManager, IEventManagerClient {

	Map<Event, List<IEventListener>> eventMap = new HashMap<Event, List<IEventListener>>();


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.events.IEventManager#register(org.ambientlight
	 * .process.events.IEventListener,
	 * org.ambientlight.process.trigger.EventTriggerConfiguration)
	 */
	@Override
	public void register(final IEventListener eventListener, final Event triggerConfig) {
		System.out.println("EventManager: registering event: " + triggerConfig.toString());
		if (triggerConfig instanceof AlarmEvent) {
			AlarmEvent alarmConfig = (AlarmEvent) triggerConfig;
			((AlarmGenerator) AmbientControlMW.getRoom().eventGenerators.get(alarmConfig.sourceName))
			.createAlarm((AlarmEvent) triggerConfig);
		}

		List<IEventListener> eventListenerList = eventMap.get(triggerConfig);
		if (eventListenerList == null) {
			eventListenerList = new ArrayList<IEventListener>();
			eventMap.put(triggerConfig, eventListenerList);
		}
		eventListenerList.add(eventListener);
	}


	/**
	 * @param process
	 * @param event
	 */
	public void unregister(IEventListener process, Event event) {
		this.eventMap.get(event).remove(process);
		if (this.eventMap.get(event).isEmpty()) {
			this.eventMap.remove(event);
		}
		System.out.println("EventManager: unregistered event:" + event.toString());
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.events.IEventManagerClient#onEvent(org.ambientlight
	 * .process.trigger.EventTriggerConfiguration, java.lang.String)
	 */
	@Override
	public void onEvent(Event event) {

		List<IEventListener> eventListeners = this.eventMap.get(event);
		if (eventListeners != null) {
			for (IEventListener currentListener : eventListeners) {
				System.out.println("EventManager: onEvent called: " + event.toString());
				currentListener.handleEvent(event);
			}
		}
	}

}
