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
import org.ambientlight.config.room.entities.scenery.SceneryManagerConfiguration;
import org.ambientlight.events.BroadcastEvent;
import org.ambientlight.events.EventListener;
import org.ambientlight.events.EventManager;
import org.ambientlight.events.SceneryEntryEvent;
import org.ambientlight.room.Persistence;
import org.ambientlight.room.entities.features.sensor.ScenerySensor;


/**
 * @author Florian Bornkessel
 * 
 * 
 */
public class SceneryManager implements ScenerySensor, EventListener {


	private SceneryManagerConfiguration config;


	private CallBackManager callbackManager;


	public SceneryManager(SceneryManagerConfiguration config, EventManager eventManager, CallBackManager callbackManager) {
		super();
		this.config = config;
		this.callbackManager = callbackManager;

		for (Scenery scenery : config.sceneries.values()) {
			SceneryEntryEvent event = new SceneryEntryEvent(SceneryEntryEvent.SOURCE_NAME, scenery.id);
			eventManager.register(this, event);
		}
	}


	private void setCurrentScenery(String scenery) {

		if (config.sceneries.containsKey(scenery) == false)
			throw new IllegalArgumentException("Scenery does not exist!");

		Persistence.beginTransaction();

		this.config.currentScenery = config.sceneries.get(scenery);

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
		source name doch wieder zurückholen???
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.events.EventListener#handleEvent(org.ambientlight.events
	 * .BroadcastEvent)
	 */
	@Override
	public void handleEvent(BroadcastEvent event) {
		this.setCurrentScenery(((SceneryEntryEvent) event).sceneryName);
	}
}
