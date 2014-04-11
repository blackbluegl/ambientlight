package org.ambientlight.events;

import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.room.entities.features.EntityId;


public class SwitchEvent extends BroadcastEvent {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.BOOLEAN)
	public boolean powerState;



	public SwitchEvent() {
		super();
	}


	public SwitchEvent(EntityId sourceId, boolean powerState) {
		super(sourceId);
		this.powerState = powerState;
	}


	@Override
	public String toString() {
		String value = "SwitchEvent: " + sourceId + " im Zustand: ";
		return powerState ? value + "ein" : value + "aus";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (powerState ? 1231 : 1237);
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
		SwitchEvent other = (SwitchEvent) obj;
		if (powerState != other.powerState)
			return false;
		return true;
	}

}
