package org.ambientlight.config.features.actor;

import org.ambientlight.config.features.Entity;
import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface Switchable extends Entity {
	public boolean getPowerState();
	public void setPowerState(boolean powerState);

}
