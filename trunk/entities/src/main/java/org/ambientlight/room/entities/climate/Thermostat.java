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

package org.ambientlight.room.entities.climate;

import org.ambientlight.room.entities.climate.util.DeviceType;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.sensor.TemperatureSensor;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author Florian Bornkessel
 */

public class Thermostat extends MaxComponent implements TemperatureSensor {

	private static final long serialVersionUID = 1L;

	private float offset;
	private boolean isLocked;
	private float temperature;


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.room.actors.MaxComponentConfiguration#getDeviceType ()
	 */
	@Override
	@JsonIgnore
	public DeviceType getDeviceType() {
		return DeviceType.HEATING_THERMOSTAT;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.sensor.Sensor#getSensorId()
	 */
	@Override
	@JsonIgnore
	public EntityId getSensorId() {
		return new EntityId(EntityId.DOMAIN_TEMP_MAX_THERMOSTATE, this.getLabel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.sensor.Sensor#getSensorValue()
	 */
	@Override
	@JsonIgnore
	public Object getSensorValue() {
		return this.temperature;
	}


	public float getOffset() {
		return offset;
	}


	public void setOffset(float offset) {
		this.offset = offset;
	}


	public boolean isLocked() {
		return isLocked;
	}


	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}


	public float getTemperature() {
		return temperature;
	}


	public void setTemperature(float messuredTemp) {
		this.temperature = messuredTemp;
	}
}
