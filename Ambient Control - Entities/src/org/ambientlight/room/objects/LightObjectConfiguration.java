package org.ambientlight.room.objects;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("lightObject")
public class LightObjectConfiguration extends RoomItemConfiguration{
	
	public int xOffsetInRoom;
	
	public int yOffsetInRoom;
	
	public int height;
	
	public int width;
	
	public int layerNumber;
	
	
}
