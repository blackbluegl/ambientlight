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
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.actor.types.SwitchType;
import org.ambientlight.room.entities.features.actor.types.SwitchableId;
import org.ambientlight.room.entities.features.sensor.ScenerySensor;
import org.ambientlight.room.entities.features.sensor.TemperatureSensor;
import org.ambientlight.room.entities.features.sensor.types.TemperatureSensorId;
import org.ambientlight.room.entities.features.sensor.types.TemperatureSensorType;
import org.ambientlight.room.entities.lightobject.LightObjectManager;


/**
 * @author Florian Bornkessel
 * 
 */
public class FeatureFacade {

	private LightObjectManager lightObjectManager;

	Map<SwitchableId, SwitchablesHandler> switchableMap = new HashMap<SwitchableId, SwitchablesHandler>();
	Map<TemperatureSensorId, TemperatureSensor> tempsensorsMap = new HashMap<TemperatureSensorId, TemperatureSensor>();

	private ScenerySensor sceneryManager;


	public void registerLightObjectManager(LightObjectManager manager) {
		lightObjectManager = manager;
	}

	public void registerTemperatureSensor(TemperatureSensor sensor, TemperatureSensorType type) {
		TemperatureSensorId id = new TemperatureSensorId();
		id.id = sensor.getSensorId();
		id.type = type;
		tempsensorsMap.put(id, sensor);
	}


	public void registerScenerySensor(ScenerySensor sensor) {
		this.sceneryManager = sensor;
	}


	public ScenerySensor getScenerySensor() {
		return this.sceneryManager;
	}


	public Map<TemperatureSensorId, TemperatureSensor> getTemperatureSensors() {
		return this.getTemperatureSensors();
	}


	public void registerSwitchable(SwitchablesHandler handler, Switchable switchable, SwitchType type) {
		SwitchableId id = new SwitchableId(switchable.getId(), type);
		switchableMap.put(id, handler);
	}


	public Set<SwitchableId> getSwitchableIds() {
		return this.switchableMap.keySet();
	}


	public Switchable getSwitchable(SwitchType type, String id) {
		SwitchableId switchId = new SwitchableId(id, type);
		return switchableMap.get(switchId).getSwitchable(switchId);
	}


	public void setSwitcheablePowerState(SwitchType type, String id, boolean powerState) {
		SwitchableId switchId = new SwitchableId(id, type);
		SwitchablesHandler handler = switchableMap.get(switchId);

		if (handler == null)
			return;

		handler.setPowerState(id, type, powerState);
	}


	public void setRenderingConfiguration(RenderingProgramConfiguration config, String id) {
		lightObjectManager.setRenderingConfiguration(config, id);
	}
}
