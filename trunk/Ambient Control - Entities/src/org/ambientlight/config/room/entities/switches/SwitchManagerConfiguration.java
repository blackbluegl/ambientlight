package org.ambientlight.config.room.entities.switches;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;



public class SwitchManagerConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	public Map<String, Switch> switches = new HashMap<String, Switch>();
}
