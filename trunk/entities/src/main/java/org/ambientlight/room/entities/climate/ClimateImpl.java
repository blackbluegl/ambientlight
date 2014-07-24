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

import org.ambientlight.config.room.entities.climate.TemperaturMode;
import org.ambientlight.room.entities.climate.util.MaxThermostateMode;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.climate.Climate;


/**
 * @author Florian Bornkessel
 * 
 */
public class ClimateImpl implements Climate {

	private static final long serialVersionUID = 1L;

	TemperaturMode mode;


	public ClimateImpl() {
		super();
	}


	public ClimateImpl(TemperaturMode mode, MaxThermostateMode modeBeforeBoost) {
		super();
		this.mode = mode;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.Entity#getId()
	 */
	@Override
	public EntityId getId() {
		return new EntityId(EntityId.DOMAIN_TEMP_MAX_CLIMATE_MANAGER, EntityId.ID_CLIMATE_MANAGER);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.climate.Climate#getClimate()
	 */
	@Override
	public TemperaturMode getTemperatureMode() {
		return mode;
	}
}
