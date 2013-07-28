package org.ambientlight.process.handler.actor;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.scenery.actor.ActorConductConfiguration;


public class ConfigurationChangeHandlerConfiguration extends AbstractActionHandlerConfiguration{

	@TypeDef(fieldType = FieldType.MAP)
	public Map<String, ActorConductConfiguration> actorConfiguration = new HashMap<String, ActorConductConfiguration>();
}
