package org.ambientlight.process.events;



public class AlarmEventConfiguration extends EventConfiguration {

	public int hour;
	public int minute;


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
