package org.ambientlight.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EventManager {

	Map<BroadcastEvent, List<EventListener>> eventMap = new HashMap<BroadcastEvent, List<EventListener>>();



	public void register(final EventListener eventListener, final BroadcastEvent triggerConfig) {
		System.out.println("EventManager: registering event: " + triggerConfig.toString());

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

		System.out.println("EventManager: onEvent called: " + event.toString());

		List<EventListener> eventListeners = this.eventMap.get(event);
		if (eventListeners != null) {
			for (EventListener currentListener : eventListeners) {
				currentListener.handleEvent(event);
				System.out.println("EventManager: triggered eventHandler: " + currentListener.getClass().getSimpleName());
			}
		}
	}

}
