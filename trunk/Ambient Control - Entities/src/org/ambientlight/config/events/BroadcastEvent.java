package org.ambientlight.config.events;

import java.io.Serializable;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.ValueBindingPath;
import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class BroadcastEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.STRING)
	@AlternativeValues(valueBinding = {
			@ValueBindingPath(forSubClass = "org.ambientlight.process.events.SceneryEntryEventConfiguration", valueBinding = "getSceneryEventGenerator().keySet()"),
			@ValueBindingPath(forSubClass = "org.ambientlight.process.events.SwitchEventConfiguration", valueBinding = "getSwitchGenerators().keySet()") })
	public String sourceId;


	public BroadcastEvent(String sourceId) {
		this.sourceId = sourceId;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceId == null) ? 0 : sourceId.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BroadcastEvent other = (BroadcastEvent) obj;
		if (sourceId == null) {
			if (other.sourceId != null)
				return false;
		} else if (!sourceId.equals(other.sourceId))
			return false;
		return true;
	}

}
