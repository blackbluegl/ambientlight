package org.ambientlight.room.objects;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("switchObject")
public class SwitchObjectConfiguration extends RoomItemConfiguration {

	public String deviceType;

	public int houseCode;

	public int switchingUnitCode;

}
