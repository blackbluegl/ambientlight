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

package org.ambientlight.climate;

import java.util.HashMap;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.room.actors.MaxComponentConfiguration;
import org.ambientlight.config.room.actors.ShutterContactConfiguration;
import org.ambientlight.config.room.actors.ThermostatConfiguration;
import org.ambientlight.messages.DispatcherType;
import org.ambientlight.room.Room;
import org.ambientlight.room.entities.MaxComponent;
import org.ambientlight.room.entities.ShutterContact;
import org.ambientlight.room.entities.Thermostat;


/**
 * @author Florian Bornkessel
 * 
 */
public class ClimateFactory {

	public void initClimateManager() {
		Room room = AmbientControlMW.getRoom();
		// init ClimateManager
		if (room.config.climate != null) {
			room.setMaxComponents(new HashMap<Integer, MaxComponent>());
			room.climateManager = new ClimateManager();
			room.climateManager.config = room.config.climate;
			room.qeueManager.registerMessageListener(DispatcherType.MAX, room.climateManager);

			for (MaxComponentConfiguration component : room.config.climate.devices.values()) {
				if (component instanceof ThermostatConfiguration) {
					Thermostat currentDevice = new Thermostat();
					currentDevice.config = component;
					room.getMaxComponents().put(currentDevice.config.adress, currentDevice);
					System.out.println("RoomFactory initRoom(): add Thermostat: " + currentDevice.config.label);
				}
				if (component instanceof ShutterContactConfiguration) {
					ShutterContact currentDevice = new ShutterContact();
					currentDevice.config = component;
					room.getMaxComponents().put(currentDevice.config.adress, currentDevice);
					System.out.println("RoomFactory initRoom(): add ShutterContact: " + currentDevice.config.label);
				}
			}
			room.climateManager.init();
			room.qeueManager.registerMessageListener(DispatcherType.MAX, room.climateManager);
			System.out.println("ClimateFactory initClimateManager(): initialized ClimateManager");
		}
	}
}
