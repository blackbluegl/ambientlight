package org.ambientlight.room;

import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface ISwitchableRoomItem {
	public boolean getPowerState();
	public void setPowerState(boolean powerState);
	public String getName();
	public void setName(String name);
}
