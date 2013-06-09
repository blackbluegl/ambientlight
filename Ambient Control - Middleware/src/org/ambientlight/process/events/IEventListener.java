package org.ambientlight.process.events;

import org.ambientlight.process.events.event.Event;
import org.ambientlight.process.trigger.EventTriggerConfiguration;


public interface IEventListener {

	public void handleEvent(Event event, EventTriggerConfiguration correlation);

}
