package org.ambientlight.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.process.events.EventConfiguration;


public class EventProcessConfiguration extends AbstractProcessConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;
	public List<EventConfiguration> eventTriggerConfigurations = new ArrayList<EventConfiguration>();

};
