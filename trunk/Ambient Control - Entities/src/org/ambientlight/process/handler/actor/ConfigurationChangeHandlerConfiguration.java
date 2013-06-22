package org.ambientlight.process.handler.actor;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.scenery.actor.ActorConductConfiguration;


public class ConfigurationChangeHandlerConfiguration extends AbstractActionHandlerConfiguration{

	public Map<String, ActorConductConfiguration> actorConfiguration = new HashMap<String, ActorConductConfiguration>();
}
