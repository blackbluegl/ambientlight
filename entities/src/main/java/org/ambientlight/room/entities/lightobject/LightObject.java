package org.ambientlight.room.entities.lightobject;

import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.actor.types.SwitchType;
import org.ambientlight.room.entities.features.lightobject.Renderable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("lightObject")
public class LightObject implements Switchable, Renderable {

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
	public String getId() {

		return id;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.Entity#setName(java.lang.String)
	 */
	@Override
	public void setId(String name) {
		this.id = name;

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


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.lightobject.Renderable#
	 * getRenderingProgrammConfiguration()
	 */
	@Override
	public RenderingProgramConfiguration getRenderingProgrammConfiguration() {
		return this.renderingProgrammConfiguration;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.lightobject.Renderable#
	 * getRenderingProgrammConfiguration
	 * (org.ambientlight.config.room.entities.lightobject
	 * .renderingprogram.RenderingProgramConfiguration)
	 */
	@Override
	public void setRenderingProgrammConfiguration(RenderingProgramConfiguration config) {
		this.renderingProgrammConfiguration = config;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.actor.Switchable#getType()
	 */
	@Override
	@JsonIgnore
	public SwitchType getType() {
		return SwitchType.LED;
	}
}
