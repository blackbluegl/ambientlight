package org.ambientlight.process.events;

import org.ambientlight.process.trigger.EventTriggerConfiguration;


public interface IEventListener {

	public void handleEvent(EventTriggerConfiguration alarm, String eventGeneratorName);

}
