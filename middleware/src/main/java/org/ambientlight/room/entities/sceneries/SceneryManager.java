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

import org.ambientlight.Manager;
import org.ambientlight.Persistence;
import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.room.entities.scenery.SceneryManagerConfiguration;
import org.ambientlight.events.EventManager;
import org.ambientlight.events.SceneryEntryEvent;
import org.ambientlight.room.entities.FeatureFacade;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.sensor.ScenerySensor;


/**
 * @author Florian Bornkessel
 * 
 * 
 */
public class SceneryManager extends Manager implements ScenerySensor {

	private SceneryManagerConfiguration config;

	private CallBackManager callbackManager;

	private EventManager eventManager;


	public SceneryManager(SceneryManagerConfiguration config, EventManager eventManager, CallBackManager callbackManager,
			FeatureFacade featureFacade, Persistence persistence) {
		super();
		this.config = config;
		this.callbackManager = callbackManager;
		this.eventManager = eventManager;
		this.persistence = persistence;

		featureFacade.registerSensor(this);
		featureFacade.registerSceneryManager(this);
	}


	public void setCurrentScenery(String scenery) {

		if (config.sceneries.containsKey(scenery) == false)
			throw new IllegalArgumentException("Scenery does not exist!");
		System.out.println("SceneryManager - setCurrentScenery: setting current scenery " + scenery);
		persistence.beginTransaction();

		this.config.currentScenery = config.sceneries.get(scenery);

		persistence.commitTransaction();

		SceneryEntryEvent event = new SceneryEntryEvent(new EntityId(EntityId.DOMAIN_SCENRERY, EntityId.ID_SCENERY_MANAGER),
				scenery);
		eventManager.onEvent(event);

		callbackManager.roomConfigurationChanged();
	}


	public void deleteScenery(String scenery) {

		if (config.sceneries.containsKey(scenery) == false)
			throw new IllegalArgumentException("Scenery does not exist!");

		persistence.beginTransaction();

		config.sceneries.remove(scenery);

		persistence.commitTransaction();

		callbackManager.roomConfigurationChanged();
	}


	public void createScenery(String scenery) {

		persistence.beginTransaction();

		Scenery newScenery = new Scenery();
		newScenery.id = scenery;

		config.sceneries.put(scenery, newScenery);

		persistence.commitTransaction();

		callbackManager.roomConfigurationChanged();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.sensor.ScenerySensor#getCurrentScenery()
	 */
	@Override
	public Object getSensorValue() {
		return config.currentScenery;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.config.features.sensor.Sensor#getSensorName()
	 */
	@Override
	public EntityId getSensorId() {
		return new EntityId(EntityId.DOMAIN_SCENRERY, EntityId.ID_SCENERY_MANAGER);
	}
}
