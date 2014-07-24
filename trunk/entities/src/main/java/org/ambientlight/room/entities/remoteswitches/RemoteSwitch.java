package org.ambientlight.room.entities.remoteswitches;

import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.sensor.SwitchSensor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("switchObject")
public class RemoteSwitch implements Switchable, SwitchSensor {

	private static final long serialVersionUID = 1L;

	public int houseCode;

	public int switchingUnitCode;

	private boolean powerstate;

	private EntityId id;


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.Entity#getName()
	 */
	@Override
	public EntityId getId() {
		return id;
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
		return this.powerstate;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.actor.Switchable#setPowerState(boolean)
	 */
	@Override
	public void setPowerState(boolean powerState) {
		this.powerstate = powerState;
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
		return getPowerState();
	}
}
