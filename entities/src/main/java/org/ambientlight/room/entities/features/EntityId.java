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

package org.ambientlight.room.entities.features;

/**
 * @author Florian Bornkessel
 * 
 */
public class EntityId {

	public static final String DOMAIN_TEMP_MAX_THERMOSTATE = "temperature.max.thermostate";
	public static final String DOMAIN_TEMP_MAX_ROOM = "temperature.max.room";
	public static final String ID_CLIMATE_MANAGER = "climateManager";
	public static final String DOMAIN_SCENRERY = "scenery";
	public static final String ID_SCENERY_MANAGER = "scenerymanager";

	public static final String DOMAIN_SWITCH_VIRTUAL = "switch.virtual";

	public static final String DOMAIN_SWITCH_VIRTUAL_MAIN = "switch.virtual.main";
	public static final String ID_SWITCH_VIRTUAL_MAIN_SWITCH = "mainswitch";

	public static final String DOMAIN_SWITCH_REMOTE = "switch.remote";

	public static final String DOMAIN_LIGHTOBJECT = "lightobject";

	public static final String DOMAIN_ALARM_DAILY = "alarm.daily";

	public String domain;
	public String id;


	public EntityId() {
		super();
	}


	public EntityId(String domain, String id) {
		super();

		this.domain = domain;
		this.id = id;
	}


	@Override
	public String toString() {
		return "Sensor ID: " + domain + "." + id;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityId other = (EntityId) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
