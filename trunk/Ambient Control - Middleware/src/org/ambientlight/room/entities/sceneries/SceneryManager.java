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

import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.events.SceneryEntryEvent;
import org.ambientlight.config.room.entities.scenery.Scenery;
import org.ambientlight.config.room.entities.scenery.SceneryManagerConfiguration;
import org.ambientlight.eventmanager.EventManager;
import org.ambientlight.room.Persistence;
import org.ambientlight.room.entities.features.sensor.ScenerySensor;


/**
 * @author Florian Bornkessel
 * 
 * 
 */
public class SceneryManager implements ScenerySensor {

	public static final String SOURCE_NAME = "SceneryManager";

	private SceneryManagerConfiguration config;

	private EventManager eventManager;

	private CallBackManager callbackManager;


	public SceneryManager(SceneryManagerConfiguration config, EventManager eventManager, CallBackManager callbackManager) {
		super();
		this.config = config;
		this.eventManager = eventManager;
		this.callbackManager = callbackManager;
	}


	public void setCurrentScenery(String scenery) {

		if (config.sceneries.containsKey(scenery) == false)
			throw new IllegalArgumentException("Scenery does not exist!");

		Persistence.beginTransaction();

		SceneryEntryEvent currentSceneryEntry = new SceneryEntryEvent(SOURCE_NAME, scenery);

		this.config.currentScenery = config.sceneries.get(scenery);

		eventManager.onEvent(currentSceneryEntry);

		Persistence.commitTransaction();

		callbackManager.roomConfigurationChanged();
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
		return SOURCE_NAME;
	}

}
