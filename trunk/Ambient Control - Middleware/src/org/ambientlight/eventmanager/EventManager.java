package org.ambientlight.eventmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.events.BroadcastEvent;
import org.ambientlight.config.events.DailyAlarmEvent;
import org.ambientlight.room.entities.AlarmGenerator;


public class EventManager implements IEventManager, IEventManagerClient {

	Map<BroadcastEvent, List<IEventListener>> eventMap = new HashMap<BroadcastEvent, List<IEventListener>>();


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.events.IEventManager#register(org.ambientlight
	 * .process.events.IEventListener,
	 * org.ambientlight.process.trigger.EventTriggerConfiguration)
	 */
	@Override
	public void register(final IEventListener eventListener, final BroadcastEvent triggerConfig) {
		System.out.println("EventManager: registering event: " + triggerConfig.toString());
		if (triggerConfig instanceof DailyAlarmEvent) {
			DailyAlarmEvent alarmConfig = (DailyAlarmEvent) triggerConfig;
			((AlarmGenerator) AmbientControlMW.getRoom().eventGenerators.get(alarmConfig.sourceId))
			.createAlarm((DailyAlarmEvent) triggerConfig);
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
	public void unregister(IEventListener process, BroadcastEvent event) {
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
	public void onEvent(BroadcastEvent event) {

		List<IEventListener> eventListeners = this.eventMap.get(event);
		if (eventListeners != null) {
			for (IEventListener currentListener : eventListeners) {
				System.out.println("EventManager: onEvent called: " + event.toString());
				currentListener.handleEvent(event);
			}
		}
	}

}
