package org.ambientlight.process.handler.actor;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.annotations.AlternativeIds;
import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.scenery.actor.ActorConductConfiguration;


public class ConfigurationChangeHandlerConfiguration extends AbstractActionHandlerConfiguration {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.MAP)
	@AlternativeIds(idBinding = "actorConfigurations.keySet()")
	@AlternativeValues(values = {
			@Value(name = "Farbe auswählen", className = "org.ambientlight.scenery.actor.renderingprogram.SimpleColorRenderingProgramConfiguration"),
			@Value(name = "Tron auswählen", className = "org.ambientlight.scenery.actor.renderingprogram.TronRenderingProgrammConfiguration") })
	public Map<String, ActorConductConfiguration> actorConfiguration = new HashMap<String, ActorConductConfiguration>();
}
