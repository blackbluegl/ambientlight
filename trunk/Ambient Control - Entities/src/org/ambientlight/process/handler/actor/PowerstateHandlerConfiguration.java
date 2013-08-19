package org.ambientlight.process.handler.actor;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.annotations.AlternativeIds;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.process.handler.DataTypeValidation;
import org.ambientlight.process.handler.HandlerDataTypeValidation;


public class PowerstateHandlerConfiguration extends AbstractActionHandlerConfiguration{

	private static final long serialVersionUID = 1L;

	@HandlerDataTypeValidation(consumes = { DataTypeValidation.CONSUMES_NO_DATA }, generates = DataTypeValidation.CREATES_NO_DATA)
	@TypeDef(fieldType = FieldType.MAP)
	@AlternativeIds(idBinding = "actorConfigurations.keySet()")
	public Map<String,Boolean> powerStateConfiguration = new HashMap<String, Boolean>();
}
