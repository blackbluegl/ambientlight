package org.ambientlight.eventmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.events.BroadcastEvent;
import org.ambientlight.config.events.DailyAlarmEvent;
import org.ambientlight.room.entities.alarm.AlarmManager;


public class EventManager {

	Map<BroadcastEvent, List<EventListener>> eventMap = new HashMap<BroadcastEvent, List<EventListener>>();



	public void register(final EventListener eventListener, final BroadcastEvent triggerConfig) {
		System.out.println("EventManager: registering event: " + triggerConfig.toString());
		if (triggerConfig instanceof DailyAlarmEvent) {
			DailyAlarmEvent alarmConfig = (DailyAlarmEvent) triggerConfig;
			((AlarmManager) AmbientControlMW.getRoom().eventGenerators.get(alarmConfig.sourceId))
			.createAlarm((DailyAlarmEvent) triggerConfig);
		}

		List<EventListener> eventListenerList = eventMap.get(triggerConfig);
		if (eventListenerList == null) {
			eventListenerList = new ArrayList<EventListener>();
			eventMap.put(triggerConfig, eventListenerList);
		}
		eventListenerList.add(eventListener);
	}


	/**
	 * @param process
	 * @param event
	 */
	public void unregister(EventListener process, BroadcastEvent event) {
		this.eventMap.get(event).remove(process);
		if (this.eventMap.get(event).isEmpty()) {
			this.eventMap.remove(event);
		}
		System.out.println("EventManager: unregistered event:" + event.toString());
	}



	public void onEvent(BroadcastEvent event) {

		List<EventListener> eventListeners = this.eventMap.get(event);
		if (eventListeners != null) {
			for (EventListener currentListener : eventListeners) {
				System.out.println("EventManager: onEvent called: " + event.toString());
				currentListener.handleEvent(event);
			}
		}
	}

}
