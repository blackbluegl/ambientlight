package org.ambientlight.process.events;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;


public class SceneryEntryEventConfiguration extends EventConfiguration {

	@TypeDef(fieldType = FieldType.STRING)
	@AlternativeValues(valueBinding = "sceneries.id")
	public String sceneryName;

	@TypeDef(fieldType = FieldType.STRING)
	@AlternativeValues(valueBinding = "getSceneryEventGenerator().keySet()")
	public String eventGeneratorName;

	@Override
	public String toString() {
		return getClass().getSimpleName() + " für " + eventGeneratorName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventGeneratorName == null) ? 0 : eventGeneratorName.hashCode());
		result = prime * result + ((sceneryName == null) ? 0 : sceneryName.hashCode());
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
		SceneryEntryEventConfiguration other = (SceneryEntryEventConfiguration) obj;
		if (eventGeneratorName == null) {
			if (other.eventGeneratorName != null)
				return false;
		} else if (!eventGeneratorName.equals(other.eventGeneratorName))
			return false;
		if (sceneryName == null) {
			if (other.sceneryName != null)
				return false;
		} else if (!sceneryName.equals(other.sceneryName))
			return false;
		return true;
	}


}
