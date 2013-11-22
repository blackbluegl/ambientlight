package org.ambientlight.process.events;




public class AlarmEvent extends Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int hour;
	public int minute;


	public AlarmEvent(int hour, int minute, String sourceName) {
		this.hour = hour;
		this.minute = minute;
		super.sourceName = sourceName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceName == null) ? 0 : sourceName.hashCode());
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
		AlarmEvent other = (AlarmEvent) obj;
		if (sourceName == null) {
			if (other.sourceName != null)
				return false;
		} else if (!sourceName.equals(other.sourceName))
			return false;
		if (hour != other.hour)
			return false;
		if (minute != other.minute)
			return false;
		return true;
	}


}
