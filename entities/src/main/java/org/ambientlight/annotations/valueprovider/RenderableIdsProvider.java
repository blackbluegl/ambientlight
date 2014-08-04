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

package org.ambientlight.annotations.valueprovider;

import java.util.ArrayList;
import java.util.Set;

import org.ambientlight.annotations.valueprovider.api.AlternativeValueProvider;
import org.ambientlight.annotations.valueprovider.api.AlternativeValues;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.ws.Room;


/**
 * This provider helps to use the map for rendering configurations and renderable ids as keys. Creates a valid list of Keys and
 * provides a list for renderingProgramm configurations.
 * 
 * @author Florian Bornkessel
 */
public class RenderableIdsProvider implements AlternativeValueProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.config.AlternativeValueProvider#getValue(java.lang.Object, java.lang.Object)
	 */
	@Override
	public AlternativeValues getValue(Room config, Object entity) {
		AlternativeValues result = new AlternativeValues();
		// create keys
		if (config.lightObjectManager != null && config.lightObjectManager.lightObjects != null) {
			Set<EntityId> keys = config.lightObjectManager.lightObjects.keySet();
			result.keys = new ArrayList<Object>(keys);
		}

		// // create new class instances
		// result.classNames.add(SimpleColorRenderingProgramConfiguration.class.getName());
		// result.displayClassNames.add("Farbe auswählen");
		// result.classNames.add(TronRenderingProgrammConfiguration.class.getName());
		// result.displayClassNames.add("Tron auswählen");
		// result.classNames.add(SunSetRenderingProgrammConfiguration.class.getName());
		// result.displayClassNames.add("Sonnenuntergang auswählen");
		return result;
	}
}
