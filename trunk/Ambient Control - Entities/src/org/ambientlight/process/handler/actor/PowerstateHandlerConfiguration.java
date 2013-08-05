package org.ambientlight.process.handler.actor;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.annotations.AlternativeIds;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;


public class PowerstateHandlerConfiguration extends AbstractActionHandlerConfiguration{

	@TypeDef(fieldType = FieldType.MAP)
	@AlternativeIds(idBinding = "actorConfigurations.keySet()")
	public Map<String,Boolean> powerStateConfiguration = new HashMap<String, Boolean>();
}
