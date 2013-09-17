package org.ambientlight.process.events;

import java.io.Serializable;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.ValueBindingPath;
import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class EventConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.STRING)

	@AlternativeValues(valueBinding = {
			@ValueBindingPath(forSubClass = "org.ambientlight.process.events.SceneryEntryEventConfiguration", valueBinding = "getSceneryEventGenerator().keySet()"),
			@ValueBindingPath(forSubClass = "org.ambientlight.process.events.SwitchEventConfiguration", valueBinding = "getSwitchGenerators().keySet()") })
	public String eventGeneratorName;

}
