package org.ambientlight.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.process.events.EventConfiguration;


public class ProcessConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;
	public String id;
	public List<EventConfiguration> eventTriggerConfigurations = new ArrayList<EventConfiguration>();
	public Map<Integer, NodeConfiguration> nodes = new HashMap<Integer, NodeConfiguration>();
};
