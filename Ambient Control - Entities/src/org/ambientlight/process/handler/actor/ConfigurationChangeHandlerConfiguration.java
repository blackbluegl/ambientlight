package org.ambientlight.process.handler.actor;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.annotations.AlternativeIds;
import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.process.handler.DataTypeValidation;
import org.ambientlight.process.handler.HandlerDataTypeValidation;
import org.ambientlight.scenery.actor.ActorConductConfiguration;


public class ConfigurationChangeHandlerConfiguration extends AbstractActionHandlerConfiguration {

	private static final long serialVersionUID = 1L;
	@HandlerDataTypeValidation(consumes = { DataTypeValidation.CONSUMES_NO_DATA }, generates = DataTypeValidation.CREATES_NO_DATA)
	@TypeDef(fieldType = FieldType.MAP)
	@AlternativeIds(idBinding = "actorConfigurations.keySet()")
	@AlternativeValues(values = {
			@Value(displayName = "Farbe auswählen", value = "org.ambientlight.scenery.actor.renderingprogram.SimpleColorRenderingProgramConfiguration"),
			@Value(displayName = "Tron auswählen", value = "org.ambientlight.scenery.actor.renderingprogram.TronRenderingProgrammConfiguration") })
	public Map<String, ActorConductConfiguration> actorConfiguration = new HashMap<String, ActorConductConfiguration>();
}
