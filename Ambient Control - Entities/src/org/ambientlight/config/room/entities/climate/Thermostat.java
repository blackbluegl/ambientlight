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

package org.ambientlight.config.room.entities.climate;

import java.util.Date;

import org.ambientlight.config.features.sensor.TemperatureSensor;
import org.ambientlight.messages.max.DeviceType;


/**
 * @author Florian Bornkessel
 */

public class Thermostat extends MaxComponent implements TemperatureSensor {

	public float offset;
	public boolean isLocked;
	private float temperature;


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.config.room.actors.MaxComponentConfiguration#getDeviceType
	 * ()
	 */
	@Override
	public DeviceType getDeviceType() {
		return DeviceType.HEATING_THERMOSTAT;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.config.features.sensor.TemperatureSensor#getTemperature
	 * ()
	 */
	@Override
	public float getTemperature() {
		// TODO Auto-generated method stub
		return this.temperature;
	}


	public void setTemperature(float messuredTemp) {
		this.temperature = messuredTemp;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.sensor.Sensor#getMessureDate()
	 */
	@Override
	public Date getMessureDate() {
		return this.lastUpdate;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.Entity#getName()
	 */
	@Override
	public String getSensorName() {
		return this.label;
	}
}
