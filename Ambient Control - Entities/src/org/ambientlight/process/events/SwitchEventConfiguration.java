package org.ambientlight.process.events;

public class SwitchEventConfiguration extends EventConfiguration {

	public boolean powerState;


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
