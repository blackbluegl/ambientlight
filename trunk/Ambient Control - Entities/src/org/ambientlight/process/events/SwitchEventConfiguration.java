package org.ambientlight.process.events;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;


public class SwitchEventConfiguration extends EventConfiguration {

	@TypeDef(fieldType = FieldType.BOOLEAN)
	public boolean powerState;


	@TypeDef(fieldType = FieldType.STRING)
	@AlternativeValues(valueBinding = "getSwitchGenerators().keySet()")
	public String eventGeneratorName;


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
		SwitchEventConfiguration other = (SwitchEventConfiguration) obj;
		if (powerState != other.powerState)
			return false;
		return true;
	}

}
