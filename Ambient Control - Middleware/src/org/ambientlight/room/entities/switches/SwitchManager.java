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

package org.ambientlight.room.entities.switches;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.events.SwitchEvent;
import org.ambientlight.config.room.entities.switches.Switch;
import org.ambientlight.config.room.entities.switches.SwitchManagerConfiguration;
import org.ambientlight.room.Persistence;


/**
 * @author Florian Bornkessel
 * 
 */
public class SwitchManager {

	public SwitchManagerConfiguration config;


	public void handleSwitchChange(String id, boolean powerState) {
		if (config.switches.containsKey(id) == false) {
			System.out.println("SwitchManager handleSwitchChange(): got request from unknown device: =" + id);
			return;
		}

		Persistence.beginTransaction();

		Switch switchConfig = config.switches.get(id);
		switchConfig.powerState = powerState;

		Persistence.commitTransaction();

		SwitchEvent switchEvent = new SwitchEvent(switchConfig.id, powerState, switchConfig.type);

		AmbientControlMW.getRoom().eventManager.onEvent(switchEvent);
	}
}
