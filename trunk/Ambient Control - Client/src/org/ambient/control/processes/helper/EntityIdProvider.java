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

package org.ambient.control.processes.helper;

import java.util.ArrayList;
import java.util.List;

import org.ambient.control.config.AlternativeValueProvider;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.ws.Room;


/**
 * @author Florian Bornkessel
 * 
 */
public class EntityIdProvider implements AlternativeValueProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.config.AlterativeValueProvider#getValue(java.lang.Object, java.lang.Object)
	 */
	@Override
	public List<String> getValue(Object base, Object entity) {
		Room room = (Room) base;
		EntityId entityId = (EntityId) entity;

		return getIdsForDomain(room, entityId.domain);
	}


	private List<String> getIdsForDomain(Room room, String domain) {
		List<String> result = new ArrayList<String>();
		for (Switchable current : room.switchables) {
			if (current.getId().domain.equals(domain)) {
				result.add(current.getId().id);
			}
		}
		return result;
	}

}
