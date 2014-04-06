package org.ambientlight.room.entities.remoteswitches;

import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.actor.Switchable;

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
	public EntityId getId() {
		return new EntityId(EntityId.DOMAIN_SWITCH_REMOTE, this.id);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.Entity#setName(java.lang.String)
	 */
	@Override
	public void setId(EntityId name) {
		this.id = name.id;
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
}
