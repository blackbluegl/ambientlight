package org.ambientlight.process.events;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;



public class AlarmEventConfiguration extends EventConfiguration {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int hour;
	public int minute;

	@TypeDef(fieldType = FieldType.STRING)
	@AlternativeValues(valueBinding = "getAlarmEventGenerator().keySet()")
	public String eventGeneratorName;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + hour;
		result = prime * result + minute;
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
		AlarmEventConfiguration other = (AlarmEventConfiguration) obj;
		if (hour != other.hour)
			return false;
		if (minute != other.minute)
			return false;
		return true;
	}

}
