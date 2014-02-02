package org.ambientlight.config.process.handler.actor;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.annotations.AlternativeIds;
import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.config.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.ws.process.validation.HandlerDataTypeValidation;


@HandlerDataTypeValidation(consumes = { DataTypeValidation.CONSUMES_NO_DATA }, generates = DataTypeValidation.CREATES_NO_DATA)
public class RenderingProgrammChangeHandlerConfiguration extends AbstractActionHandlerConfiguration {

	private static final long serialVersionUID = 1L;
	@TypeDef(fieldType = FieldType.MAP)
	@AlternativeIds(idBinding = "actorConfigurations.keySet()")
	@AlternativeValues(values = {
			@Value(displayName = "Farbe auswählen", value = "org.ambientlight.scenery.actor.renderingprogram.SimpleColorRenderingProgramConfiguration"),
			@Value(displayName = "Tron auswählen", value = "org.ambientlight.scenery.actor.renderingprogram.TronRenderingProgrammConfiguration") })
	public Map<String, RenderingProgramConfiguration> renderConfig = new HashMap<String, RenderingProgramConfiguration>();
}
