/*  Copyright 2013 Florian Bornkessel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.ambientlight.room.entities.switches;

import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.sensor.SwitchSensor;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author Florian Bornkessel
 * 
 */
public class Switch implements Switchable, SwitchSensor {

	private static final long serialVersionUID = 1L;

	private EntityId id;
	private boolean powerState;


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.room.SwitchableActor#getPowerState()
	 */
	@Override
	public boolean getPowerState() {
		return this.powerState;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.room.SwitchableActor#setPowerState(boolean)
	 */
	@Override
	public void setPowerState(boolean powerState) {
		this.powerState = powerState;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.room.SwitchableActor#getName()
	 */
	@Override
	public EntityId getId() {
		return this.id;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.room.SwitchableActor#setName(java.lang.String)
	 */
	@Override
	public void setId(EntityId id) {
		this.id = id;
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
