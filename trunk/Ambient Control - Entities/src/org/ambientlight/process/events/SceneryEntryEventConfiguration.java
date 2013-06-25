package org.ambientlight.process.events;


public class SceneryEntryEventConfiguration extends EventConfiguration {
	public String sceneryName;


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((sceneryName == null) ? 0 : sceneryName.hashCode());
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
		SceneryEntryEventConfiguration other = (SceneryEntryEventConfiguration) obj;
		if (sceneryName == null) {
			if (other.sceneryName != null)
				return false;
		} else if (!sceneryName.equals(other.sceneryName))
			return false;
		return true;
	}

}
