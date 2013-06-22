package org.ambientlight.process.eventmanager;

import org.ambientlight.process.events.Event;
import org.ambientlight.process.trigger.EventTriggerConfiguration;


public interface IEventListener {

	public void handleEvent(Event event, EventTriggerConfiguration correlation);

}
