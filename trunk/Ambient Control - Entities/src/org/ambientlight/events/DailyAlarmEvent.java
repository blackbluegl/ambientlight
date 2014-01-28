package org.ambientlight.events;

public class DailyAlarmEvent extends BroadcastEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int hour;
	public int minute;


	public DailyAlarmEvent(String sourceId, int hour, int minute) {
		super(sourceId);
		this.hour = hour;
		this.minute = minute;
	}


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
		DailyAlarmEvent other = (DailyAlarmEvent) obj;
		if (hour != other.hour)
			return false;
		if (minute != other.minute)
			return false;
		return true;
	}

}
