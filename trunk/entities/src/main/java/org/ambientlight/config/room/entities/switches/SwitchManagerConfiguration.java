package org.ambientlight.config.room.entities.switches;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ambientlight.room.entities.switches.Switch;



public class SwitchManagerConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	public Map<String, Switch> switches = new HashMap<String, Switch>();
}
