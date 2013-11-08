package org.ambientlight.process.events;

import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;


public class NFCTagSwitchEventConfiguration extends EventConfiguration {

	private static final long serialVersionUID = 1L;
	@TypeDef(fieldType = FieldType.BOOLEAN)
	public boolean powerState;



	@Override
	public String toString() {
		String value = "NFCTag-Schalter: " + eventGeneratorName + " im Zustand: ";
		return powerState ? value + "ein" : value + "aus";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventGeneratorName == null) ? 0 : eventGeneratorName.hashCode());
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
		NFCTagSwitchEventConfiguration other = (NFCTagSwitchEventConfiguration) obj;
		if (eventGeneratorName == null) {
			if (other.eventGeneratorName != null)
				return false;
		} else if (!eventGeneratorName.equals(other.eventGeneratorName))
			return false;
		if (powerState != other.powerState)
			return false;
		return true;
	}


}
