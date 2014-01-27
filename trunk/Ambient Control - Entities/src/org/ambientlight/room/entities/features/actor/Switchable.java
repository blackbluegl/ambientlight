package org.ambientlight.room.entities.features.actor;

import org.ambientlight.room.entities.features.Entity;
import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface Switchable extends Entity {

	public boolean getPowerState();


	public void setPowerState(boolean powerState);

}
