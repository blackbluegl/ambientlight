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

import org.ambientlight.annotations.valueprovider.api.AlternativeValueProvider;
import org.ambientlight.annotations.valueprovider.api.AlternativeValues;
import org.ambientlight.ws.Room;


/**
 * @author Florian Bornkessel
 * 
 */
public class SwitchesIdsProvider implements AlternativeValueProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.annotations.valueprovider.api.AlternativeValueProvider#getValue(org.ambientlight.ws.Room,
	 * java.lang.Object)
	 */
	@Override
	public AlternativeValues getValue(Room config, Object entity) {

		AlternativeValues result = new AlternativeValues();
		result.values = new ArrayList<Object>();

		if (config.switchesManager != null && config.switchesManager.switches != null) {
			result.values.addAll(config.switchesManager.switches.keySet());
		}
		if (config.remoteSwitchesManager != null && config.remoteSwitchesManager.remoteSwitches != null) {
			result.values.addAll(config.remoteSwitchesManager.remoteSwitches.keySet());
		}
		if (config.lightObjectManager != null && config.lightObjectManager.lightObjects.keySet() != null) {
			result.values.addAll(config.lightObjectManager.lightObjects.keySet());
		}

		return result;
	}
}
