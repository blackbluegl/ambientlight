package org.ambientlight.config.process.events;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.ValueBindingPath;


public class SceneryEntryEvent extends Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@TypeDef(fieldType = FieldType.STRING)
	@AlternativeValues(valueBinding = { @ValueBindingPath(valueBinding = "getSceneries().id") })
	public String sceneryName;



	@Override
	public String toString() {
		return "Szenario: " + sceneryName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceName == null) ? 0 : sourceName.hashCode());
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
		SceneryEntryEvent other = (SceneryEntryEvent) obj;
		if (sourceName == null) {
			if (other.sourceName != null)
				return false;
		} else if (!sourceName.equals(other.sourceName))
			return false;
		if (sceneryName == null) {
			if (other.sceneryName != null)
				return false;
		} else if (!sceneryName.equals(other.sceneryName))
			return false;
		return true;
	}


}
