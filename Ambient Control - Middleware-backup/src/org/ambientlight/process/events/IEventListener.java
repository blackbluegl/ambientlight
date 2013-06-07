package org.ambientlight.process.events;

import org.ambientlight.room.eventgenerator.EventGeneratorConfiguration;


public interface IEventListener {
	public void handleEvent(EventGeneratorConfiguration configuration);
}
