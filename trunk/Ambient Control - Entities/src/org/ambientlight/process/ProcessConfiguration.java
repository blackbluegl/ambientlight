package org.ambientlight.process;

import java.beans.EventHandler;
import java.util.HashMap;
import java.util.Map;

import org.ambientlight.process.trigger.EventTriggerConfiguration;


public class ProcessConfiguration {
	public String id;
	public EventTriggerConfiguration eventTriggerConfiguration;
	public Map<Integer, NodeConfiguration> nodes = new HashMap<Integer, NodeConfiguration>();
};
