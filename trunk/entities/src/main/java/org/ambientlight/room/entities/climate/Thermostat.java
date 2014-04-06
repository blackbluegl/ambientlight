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


/**
 * @author Florian Bornkessel
 */

public class Thermostat extends MaxComponent implements TemperatureSensor {

	private static final long serialVersionUID = 1L;

	public float offset;
	public boolean isLocked;
	private float temperature;


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.room.actors.MaxComponentConfiguration#getDeviceType ()
	 */
	@Override
	public DeviceType getDeviceType() {
		return DeviceType.HEATING_THERMOSTAT;
	}


	public void setTemperature(float messuredTemp) {
		this.temperature = messuredTemp;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.sensor.Sensor#getSensorId()
	 */
	@Override
	public EntityId getSensorId() {
		return new EntityId(EntityId.DOMAIN_TEMP_MAX_THERMOSTATE, this.label);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.sensor.Sensor#getSensorValue()
	 */
	@Override
	public Object getSensorValue() {
		return this.temperature;
	}

}
