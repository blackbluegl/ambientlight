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

package org.ambientlight.process.events.generator;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.events.event.SceneryEvent;
import org.ambientlight.process.trigger.SceneryEntryEventTriggerConfiguration;
import org.ambientlight.scenery.UserSceneryConfiguration;


/**
 * @author Florian Bornkessel
 * 
 * 
 */
public class SceneryEventGenerator extends EventGenerator {


	public void sceneryEntryEventOccured(SceneryEvent event) {

		SceneryEntryEventTriggerConfiguration correlation = new SceneryEntryEventTriggerConfiguration();
		correlation.eventGeneratorName = config.name;
		correlation.sceneryName = event.sceneryName;

		UserSceneryConfiguration currentScenery = new UserSceneryConfiguration();
		currentScenery.id = event.sceneryName;
		AmbientControlMW.getRoom().config.currentSceneryConfig = currentScenery;

		eventManager.onEvent(event, correlation);
	}
}
