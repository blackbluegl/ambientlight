package org.ambientlight.config.room.entities.remoteswitches;


import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("switchObject")
public class RemoteSwitch {

	private static final long serialVersionUID = 1L;

	public String deviceType;

	public int houseCode;

	public int switchingUnitCode;

}
