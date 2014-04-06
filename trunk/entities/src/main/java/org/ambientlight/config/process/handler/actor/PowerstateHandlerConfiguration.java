package org.ambientlight.config.process.handler.actor;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.annotations.AlternativeIds;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.config.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.ws.EntityIdDeserializer;
import org.ambientlight.ws.process.validation.HandlerDataTypeValidation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


@HandlerDataTypeValidation(consumes = { DataTypeValidation.CONSUMES_NO_DATA }, generates = DataTypeValidation.CREATES_NO_DATA)
public class PowerstateHandlerConfiguration extends AbstractActionHandlerConfiguration{

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.MAP)
	@AlternativeIds(idBinding = "actorConfigurations.keySet()")
	@JsonDeserialize(keyUsing = EntityIdDeserializer.class)
	public Map<EntityId, Boolean> powerStateConfiguration = new HashMap<EntityId, Boolean>();
}
