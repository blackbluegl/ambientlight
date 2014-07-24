package org.ambientlight.room.entities.lightobject;

import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.lightobject.Renderable;
import org.ambientlight.room.entities.features.sensor.SwitchSensor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("lightObject")
public class LightObject implements Switchable, Renderable, SwitchSensor {

	private static final long serialVersionUID = 1L;

	public int xOffsetInRoom;

	public int yOffsetInRoom;

	public int height;

	public int width;

	public int layerNumber;

	private boolean powerState;

	private EntityId id;

	private RenderingProgramConfiguration renderingProgrammConfiguration;


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.Entity#getName()
	 */
	@Override
	public EntityId getId() {
		return this.id;
	}


	public void setId(EntityId name) {
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
	 * @see org.ambientlight.config.features.actor.Switchable#setPowerState(boolean)
	 */
	@Override
	public void setPowerState(boolean powerState) {
		this.powerState = powerState;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.lightobject.Renderable# getRenderingProgrammConfiguration()
	 */
	@Override
	public RenderingProgramConfiguration getRenderingProgrammConfiguration() {
		return this.renderingProgrammConfiguration;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.lightobject.Renderable# getRenderingProgrammConfiguration
	 * (org.ambientlight.config.room.entities.lightobject .renderingprogram.RenderingProgramConfiguration)
	 */
	@Override
	public void setRenderingProgrammConfiguration(RenderingProgramConfiguration config) {
		this.renderingProgrammConfiguration = config;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.sensor.Sensor#getSensorId()
	 */
	@Override
	@JsonIgnore
	public EntityId getSensorId() {
		return id;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.sensor.Sensor#getSensorValue()
	 */
	@Override
	@JsonIgnore
	public Object getSensorValue() {
		return powerState;
	}
}
