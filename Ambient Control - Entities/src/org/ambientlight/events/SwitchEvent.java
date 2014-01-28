package org.ambientlight.events;

import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;


public class SwitchEvent extends BroadcastEvent {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.BOOLEAN)
	public boolean powerState;

	public SwitchEventType type;


	public SwitchEvent(String sourceId, boolean powerState, SwitchEventType type) {
		super(sourceId);
		this.powerState = powerState;
		this.type = type;
	}


	@Override
	public String toString() {
		String value = "Schalter(" + type + "): " + sourceId + " im Zustand: ";
		return powerState ? value + "ein" : value + "aus";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (powerState ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		if (type != other.type)
			return false;
		return true;
	}

}
