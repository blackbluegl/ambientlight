package org.ambientlight.room.entities.remoteswitches;

import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.actor.types.SwitchType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("switchObject")
public class RemoteSwitch implements Switchable {

	private static final long serialVersionUID = 1L;

	public int houseCode;

	public int switchingUnitCode;

	private boolean powerstate;

	private String id;


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
		return this.powerstate;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.config.features.actor.Switchable#setPowerState(boolean)
	 */
	@Override
	public void setPowerState(boolean powerState) {
		this.powerstate = powerState;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.actor.Switchable#getType()
	 */
	@Override
	@JsonIgnore
	public SwitchType getType() {
		return SwitchType.ELRO;
	}

}
