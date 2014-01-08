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

package org.ambientlight.room.entities.sceneries;

import org.ambientlight.config.events.SceneryEntryEvent;
import org.ambientlight.config.features.sensor.ScenerySensor;
import org.ambientlight.config.room.entities.scenery.Scenery;
import org.ambientlight.config.room.entities.scenery.SceneryManagerConfiguration;
import org.ambientlight.eventmanager.EventManager;


/**
 * @author Florian Bornkessel
 * 
 * 
 */
public class SceneryManager implements ScenerySensor {

	public static final String SENSOR_NAME = "SceneryManager";

	public SceneryManagerConfiguration config;

	public EventManager eventManager;


	public void setCurrentScenery(String scenery) {
		todo Scenery als eingabe - umwandlung in event und ausgabe
		todo transactoins einfügen
		Scenery currentScenery = new Scenery();
		currentScenery.id = event.sceneryName;
		this.config.currentScenery = currentScenery;

		eventManager.onEvent(event);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.config.features.sensor.ScenerySensor#getCurrentScenery()
	 */
	@Override
	public Scenery getCurrentScenery() {
		return config.currentScenery;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.sensor.Sensor#getSensorName()
	 */
	@Override
	public String getSensorName() {
		return SENSOR_NAME;
	}

}
