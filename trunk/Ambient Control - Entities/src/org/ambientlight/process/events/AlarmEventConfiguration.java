package org.ambientlight.process.events;




public class AlarmEventConfiguration extends EventConfiguration {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int hour;
	public int minute;


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventGeneratorName == null) ? 0 : eventGeneratorName.hashCode());
		result = prime * result + hour;
		result = prime * result + minute;
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
		AlarmEventConfiguration other = (AlarmEventConfiguration) obj;
		if (eventGeneratorName == null) {
			if (other.eventGeneratorName != null)
				return false;
		} else if (!eventGeneratorName.equals(other.eventGeneratorName))
			return false;
		if (hour != other.hour)
			return false;
		if (minute != other.minute)
			return false;
		return true;
	}


}
