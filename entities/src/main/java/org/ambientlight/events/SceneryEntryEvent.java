package org.ambientlight.events;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.annotations.valueprovider.SceneryNamesProvider;
import org.ambientlight.room.entities.features.EntityId;


public class SceneryEntryEvent extends BroadcastEvent {

	public SceneryEntryEvent() {
		super();
	}


	public SceneryEntryEvent(EntityId sourceId, String sceneryName) {
		super(sourceId);
		this.sceneryName = sceneryName;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@TypeDef(fieldType = FieldType.STRING)
	@AlternativeValues(values = { @Value(valueProvider = SceneryNamesProvider.class) })
	public String sceneryName;


	@Override
	public String toString() {
		return "Szenario Event: " + sceneryName;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((sceneryName == null) ? 0 : sceneryName.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SceneryEntryEvent other = (SceneryEntryEvent) obj;
		if (sceneryName == null) {
			if (other.sceneryName != null)
				return false;
		} else if (!sceneryName.equals(other.sceneryName))
			return false;
		return true;
	}

}
