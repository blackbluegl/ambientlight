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

import org.ambientlight.config.process.events.Event;
import org.ambientlight.config.process.events.SceneryEntryEvent;
import org.ambientlight.config.room.eventgenerator.SceneryEventGeneratorConfiguration;
import org.ambientlight.config.scenery.UserSceneryConfiguration;


/**
 * @author Florian Bornkessel
 * 
 * 
 */
public class SceneryEventGenerator extends EventGenerator implements EventSensor {

	public void sceneryEntryEventOccured(SceneryEntryEvent event) {


		UserSceneryConfiguration currentScenery = new UserSceneryConfiguration();
		currentScenery.id = event.sceneryName;
		((SceneryEventGeneratorConfiguration) this.config).currentScenery = currentScenery;

		eventManager.onEvent(event);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.Sensor#getValue()
	 */
	@Override
	public Event getValue() {
		SceneryEntryEvent event = new SceneryEntryEvent();
		event.sceneryName = ((SceneryEventGeneratorConfiguration) this.config).currentScenery.id;
		event.sourceName = ((SceneryEventGeneratorConfiguration) this.config).name;
		return event;
	}
}
