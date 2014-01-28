package org.ambientlight.events;

import org.ambientlight.events.BroadcastEvent;


public interface EventListener {

	public void handleEvent(BroadcastEvent event);
}
