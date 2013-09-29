package org.ambientlight.room.actors;


import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("switchObject")
public class SwitchObjectConfiguration extends ActorConfiguration {

	public String deviceType;

	public int houseCode;

	public int switchingUnitCode;

}
