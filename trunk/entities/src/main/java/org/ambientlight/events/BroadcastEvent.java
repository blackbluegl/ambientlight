package org.ambientlight.events;

import java.io.Serializable;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.room.entities.features.EntityId;

import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class BroadcastEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.BEAN_SELECTION)
	@AlternativeValues(values = {
			@Value(forSubClass = "org.ambientlight.events.SceneryEntryEvent", valueProvider = "org.ambientlight.annotations.valueprovider.SceneryEventIdProvider"),
			@Value(forSubClass = "org.ambientlight.events.SwitchEvent", valueProvider = "org.ambientlight.annotations.valueprovider.SwitchesIdsProvider") })
	public EntityId sourceId;


	public BroadcastEvent() {
		super();
	}


	public BroadcastEvent(EntityId sourceId) {
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
