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

package org.ambientlight.room.entities;

import org.ambientlight.process.events.EventConfiguration;
import org.ambientlight.process.events.NFCTagSwitchEventConfiguration;
import org.ambientlight.room.eventgenerator.NFCTagSwitchEventGeneratorConfiguration;


/**
 * @author Florian Bornkessel
 * 
 */
public class NFCTagSwitchEventGenerator extends EventGenerator implements EventSensor {

	public void switchEventOccured(NFCTagSwitchEventConfiguration event) {
		NFCTagSwitchEventConfiguration correlation = new NFCTagSwitchEventConfiguration();
		correlation.eventGeneratorName = config.name;
		eventManager.onEvent(event);

		((NFCTagSwitchEventGeneratorConfiguration) this.config).setPowerState(event.powerState);

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.EventSensor#getValue()
	 */
	@Override
	public EventConfiguration getValue() {
		// TODO Auto-generated method stub
		NFCTagSwitchEventConfiguration config = new NFCTagSwitchEventConfiguration();
		config.eventGeneratorName = this.config.name;
		config.powerState = ((NFCTagSwitchEventGeneratorConfiguration) this.config).getPowerState();
		return config;
	}
}
