package org.ambientlight.config.room.actors;


import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("lightObject")
public class LightObjectConfiguration extends ActorConfiguration{


	public int xOffsetInRoom;

	public int yOffsetInRoom;

	public int height;

	public int width;

	public int layerNumber;


}
