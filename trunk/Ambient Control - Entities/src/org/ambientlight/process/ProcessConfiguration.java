package org.ambientlight.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.process.events.EventConfiguration;


public class ProcessConfiguration {
	public String id;
	public List<EventConfiguration> eventTriggerConfigurations = new ArrayList<EventConfiguration>();
	public Map<Integer, NodeConfiguration> nodes = new HashMap<Integer, NodeConfiguration>();
};
