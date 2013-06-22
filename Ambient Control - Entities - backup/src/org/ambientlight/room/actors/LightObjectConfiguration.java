package org.ambientlight.room.actors;


import org.ambientlight.scenery.actor.renderingprogram.RenderingProgramConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("lightObject")
public class LightObjectConfiguration extends ActorConfiguration{

	public RenderingProgramConfiguration renderingProgramConfiguration;

	public int xOffsetInRoom;

	public int yOffsetInRoom;

	public int height;

	public int width;

	public int layerNumber;


}
