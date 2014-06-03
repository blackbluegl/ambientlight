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

package org.ambient.control.home.mapper;

import org.ambient.control.R;
import org.ambientlight.room.entities.features.EntityId;

import android.view.View;


/**
 * @author Florian Bornkessel
 *
 */
public class ClimateItemViewMapper extends AbstractRoomItemViewMapper {

	/**
	 * @param itemView
	 * @param entityId
	 * @param resourceId
	 * @param powerState
	 */
	public ClimateItemViewMapper(View itemView, EntityId entityId, int resourceId, boolean powerState) {
		super(itemView, entityId, resourceId, powerState);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.home.mapper.AbstractRoomItemViewMapper#getActiveIcon()
	 */
	@Override
	protected int getActiveIcon() {
		return R.drawable.ic_heating_active;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.home.mapper.AbstractRoomItemViewMapper#getDisabledIcon()
	 */
	@Override
	protected int getDisabledIcon() {
		return R.drawable.ic_heating_disabled;
	}

}
