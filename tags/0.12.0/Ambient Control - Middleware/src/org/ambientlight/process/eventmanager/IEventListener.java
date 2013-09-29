package org.ambientlight.process.eventmanager;

import org.ambientlight.process.events.EventConfiguration;


public interface IEventListener {

	public void handleEvent(EventConfiguration event);

}
