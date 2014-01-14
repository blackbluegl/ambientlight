package org.ambientlight.eventmanager;

import org.ambientlight.config.events.BroadcastEvent;


public interface EventListener {

	public void handleEvent(BroadcastEvent event);
}
