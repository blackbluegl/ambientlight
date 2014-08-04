package org.ambientlight.config.process.handler.actor;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.annotations.valueprovider.RenderableIdsProvider;
import org.ambientlight.config.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.ws.EntityIdDeserializer;
import org.ambientlight.ws.EntityIdSerializer;
import org.ambientlight.ws.process.validation.HandlerDataTypeValidation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@HandlerDataTypeValidation(consumes = { DataTypeValidation.CONSUMES_NO_DATA }, generates = DataTypeValidation.CREATES_NO_DATA)
public class RenderingProgrammChangeHandlerConfiguration extends AbstractActionHandlerConfiguration {

	private static final long serialVersionUID = 1L;
	@TypeDef(fieldType = FieldType.MAP)
	@AlternativeValues(values = { @Value(valueProvider = RenderableIdsProvider.class) })
	@JsonSerialize(keyUsing = EntityIdSerializer.class)
	@JsonDeserialize(keyUsing = EntityIdDeserializer.class)
	public Map<EntityId, RenderingProgramConfiguration> renderConfig = new HashMap<EntityId, RenderingProgramConfiguration>();
}
