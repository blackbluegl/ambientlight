package org.ambientlight.process.events;

import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public  abstract class EventConfiguration{

	public String eventGeneratorName;


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventGeneratorName == null) ? 0 : eventGeneratorName.hashCode());
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
		EventConfiguration other = (EventConfiguration) obj;
		if (eventGeneratorName == null) {
			if (other.eventGeneratorName != null)
				return false;
		} else if (!eventGeneratorName.equals(other.eventGeneratorName))
			return false;
		return true;
	}

}
