package org.ambientlight.process.eventmanager;

import org.ambientlight.process.events.Event;


public interface IEventListener {

	public void handleEvent(Event event);

}
