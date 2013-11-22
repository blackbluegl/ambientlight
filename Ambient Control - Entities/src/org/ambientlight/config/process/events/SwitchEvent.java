package org.ambientlight.config.process.events;

import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;


public class SwitchEvent extends Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@TypeDef(fieldType = FieldType.BOOLEAN)
	public boolean powerState;



	@Override
	public String toString() {
		String value = "Schalter: " + sourceName + " im Zustand: ";
		return powerState ? value + "ein" : value + "aus";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceName == null) ? 0 : sourceName.hashCode());
		result = prime * result + (powerState ? 1231 : 1237);
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
		SwitchEvent other = (SwitchEvent) obj;
		if (sourceName == null) {
			if (other.sourceName != null)
				return false;
		} else if (!sourceName.equals(other.sourceName))
			return false;
		if (powerState != other.powerState)
			return false;
		return true;
	}


}
