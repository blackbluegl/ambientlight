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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.room.entities.climate.ClimateImpl;
import org.ambientlight.room.entities.climate.ClimateManager;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.climate.Climate;
import org.ambientlight.room.entities.features.climate.TemperaturMode;
import org.ambientlight.room.entities.features.sensor.Sensor;
import org.ambientlight.room.entities.lightobject.LightObjectManager;
import org.ambientlight.room.entities.sceneries.SceneryManager;


/**
 * @author Florian Bornkessel
 * 
 */
public class FeatureFacade {

	private ClimateManager climateManager;

	private LightObjectManager lightObjectManager;
	private SceneryManager sceneryManager;

	Map<EntityId, SwitchablesHandler> switchableMap = new HashMap<EntityId, SwitchablesHandler>();
	Map<EntityId, Sensor> sensors = new HashMap<EntityId, Sensor>();


	public void registerClimateManager(ClimateManager manager) {
		climateManager = manager;
	}


	public void registerSceneryManager(SceneryManager manager) {
		this.sceneryManager = manager;
	}


	public void setCurrentScenery(String scenery) {
		this.sceneryManager.setCurrentScenery(scenery);
	}


	public void registerSensor(Sensor sensor) {
		sensors.put(sensor.getSensorId(), sensor);
	}


	public Set<EntityId> getSensorsList() {
		return this.sensors.keySet();
	}


	public Sensor getSensor(EntityId id) {
		return this.sensors.get(id);
	}


	public void registerSwitchable(SwitchablesHandler handler, Switchable switchable) {
		switchableMap.put(switchable.getId(), handler);
	}


	public Set<EntityId> getSwitchableIds() {
		return this.switchableMap.keySet();
	}


	public Switchable getSwitchable(EntityId id) {
		return switchableMap.get(id).getSwitchable(id);
	}


	public void setSwitcheablePowerState(EntityId id, boolean powerState, boolean fireEvent) {
		SwitchablesHandler handler = switchableMap.get(id);

		if (handler == null)
			return;

		handler.setPowerState(id, powerState, fireEvent);
	}


	public void registerLightObjectManager(LightObjectManager manager) {
		lightObjectManager = manager;
	}


	public Set<EntityId> getRenderables() {
		return lightObjectManager.getRenderables();
	}


	public void setRenderingConfiguration(RenderingProgramConfiguration config, EntityId id) {
		lightObjectManager.setRenderingConfiguration(config, id);
	}


	public Climate getClimate(){
		return new ClimateImpl(climateManager.getMode());
	}


	public void setClimate(TemperaturMode mode) {
		climateManager.setMode(mode.temp, mode.mode, mode.until);
	}
}
