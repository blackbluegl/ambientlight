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

import org.ambientlight.room.entities.climate.util.MaxThermostateMode;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.climate.Climate;
import org.ambientlight.room.entities.features.climate.TemperaturMode;


/**
 * @author Florian Bornkessel
 * 
 */
public class ClimateImpl implements Climate {

	private static final long serialVersionUID = 1L;

	TemperaturMode mode;

	MaxThermostateMode thermostateModeBeforeBoost;


	public ClimateImpl() {
		super();
	}


	public ClimateImpl(TemperaturMode mode, MaxThermostateMode modeBeforeBoost) {
		super();
		this.mode = mode;
		this.thermostateModeBeforeBoost = modeBeforeBoost;
		this.setId(new EntityId(EntityId.DOMAIN_TEMP_MAX, EntityId.ID_CLIMATE_MANAGER));
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.Entity#getId()
	 */
	@Override
	public EntityId getId() {
		return new EntityId(EntityId.DOMAIN_TEMP_MAX, EntityId.ID_CLIMATE_MANAGER);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.Entity#setId(org.ambientlight.room.entities.features.EntityId)
	 */
	@Override
	public void setId(EntityId name) {
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.climate.Climate#getClimate()
	 */
	@Override
	public TemperaturMode getClimate() {
		return mode;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.climate.Climate#setClimate(org.ambientlight.room.entities.features.climate.
	 * TemperaturMode)
	 */
	@Override
	public void setClimate(TemperaturMode climate) {
		mode = climate;

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.climate.Climate#getThermostateModeBeforeBoost()
	 */
	@Override
	public MaxThermostateMode getThermostateModeBeforeBoost() {
		// TODO Auto-generated method stub
		return null;
	}

}
