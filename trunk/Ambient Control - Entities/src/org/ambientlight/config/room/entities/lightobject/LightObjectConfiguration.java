package org.ambientlight.config.room.entities.lightobject;

import java.io.Serializable;

import org.ambientlight.config.features.actor.Renderable;
import org.ambientlight.config.features.actor.Switchable;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("lightObject")
public class LightObjectConfiguration implements Switchable, Renderable, Serializable {

	private static final long serialVersionUID = 1L;

	public int xOffsetInRoom;

	public int yOffsetInRoom;

	public int height;

	public int width;

	public int layerNumber;

	private boolean powerState;

	private String id;

	private RenderingProgramConfiguration renderingProgrammConfiguration;


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.Entity#getName()
	 */
	@Override
	public String getName() {

		return id;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.Entity#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.id = name;

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.actor.Renderable#
	 * getRenderingProgramConfiguration()
	 */
	@Override
	public RenderingProgramConfiguration getRenderingProgramConfiguration() {
		return this.renderingProgrammConfiguration;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.config.features.actor.Renderable#setRenderProgram(org
	 * .ambientlight
	 * .config.room.entities.led.renderingprogram.RenderingProgramConfiguration)
	 */
	@Override
	public void setRenderProgram(RenderingProgramConfiguration programConfig) {
		this.renderingProgrammConfiguration = programConfig;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.actor.Switchable#getPowerState()
	 */
	@Override
	public boolean getPowerState() {
		return this.powerState;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.config.features.actor.Switchable#setPowerState(boolean)
	 */
	@Override
	public void setPowerState(boolean powerState) {
		this.powerState = powerState;
	}

}
