package org.ambientlight.room.actors;

import org.ambientlight.room.IUserRoomItem;
import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class ActorConfiguration implements IUserRoomItem {

	private boolean powerState;
	private String name;


	public boolean getPowerState() {
		return this.powerState;
	}


	public void setPowerState(boolean powerState) {
		this.powerState = powerState;
	}


	public String getName() {
		return this.name;
	}


	public void setName(String name) {
		this.name = name;
	}
}
