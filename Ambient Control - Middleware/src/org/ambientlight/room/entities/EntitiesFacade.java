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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.messages.max.MaxThermostateMode;
import org.ambientlight.room.entities.climate.ClimateManager;
import org.ambientlight.room.entities.features.actor.Renderable;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.actor.types.SwitchType;
import org.ambientlight.room.entities.features.sensor.ScenerySensor;
import org.ambientlight.room.entities.features.sensor.TemperatureSensor;
import org.ambientlight.room.entities.features.sensor.types.TemperatureSensorType;


/**
 * @author Florian Bornkessel
 * 
 */
public class EntitiesFacade {

	is entity facade  a facade for all entities? or better just for features? eg. featureFacade
			shall scenariosensor exist? are we really needing it?
					is renderable a feature? or should it just be handled direct in the lightObjectmanager?

							Map<String, RenderableHandler> renderableMap = new HashMap<String, RenderableHandler>();
			Map<SwitcheableId, SwitchablesHandler> switchableMap = new HashMap<SwitcheableId, SwitchablesHandler>();
			Map<TemperatureSensorId, TemperatureSensor> tempsensorsMap = new HashMap<TemperatureSensorId, TemperatureSensor>();
			private ClimateManager climateManager;
			private ScenerySensor sceneryManager;


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
				SwitcheableId id = new SwitcheableId();
				id.id = switchable.getId();
				id.type = type;

				switchableMap.put(id, handler);
			}


			public void registerRenderable(RenderableHandler handler, Renderable renderable) {
				this.renderableMap.put(renderable.getId(), handler);
			}


			public void registerClimateManager(ClimateManager climateManager) {
				this.climateManager = climateManager;
			}


			public void setClimateMode(float temp, MaxThermostateMode mode, Date until) {
				climateManager.setMode(temp, mode, until);
			}


			public void setClimateCurrentWeekProfile(String profile) {
				climateManager.setCurrentProfile(profile);
			}


			public void setLightObjectRenderingProgrammConfiguration(String id, RenderingProgramConfiguration config) {
				RenderableHandler handler = renderableMap.get(id);
				if (handler == null)
					return;

				handler.setRenderingProgrammConfiguration(id, config);
			}


			public void setSwitcheablePowerState(SwitchType type, String id, boolean powerState) {
				SwitcheableId switchId = new SwitcheableId();
				switchId.id = id;
				switchId.type = type;
				SwitchablesHandler handler = switchableMap.get(switchId);

				if (handler == null)
					return;

				handler.setPowerState(id, type, powerState);
			}
}
