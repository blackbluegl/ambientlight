package org.ambientlight.eventmanager;

import org.ambientlight.config.events.BroadcastEvent;


public interface IEventListener {

	public void handleEvent(BroadcastEvent event);
}
