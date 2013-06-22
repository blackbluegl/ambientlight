package org.ambientlight.process.handler.actor;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;


public class PowerstateHandlerConfiguration extends AbstractActionHandlerConfiguration{
	public Map<String,Boolean> powerStateConfiguration = new HashMap<String, Boolean>();
}
