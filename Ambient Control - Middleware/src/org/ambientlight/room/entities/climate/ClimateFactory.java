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

package org.ambientlight.room.entities.climate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.config.room.entities.climate.MaxComponentConfiguration;
import org.ambientlight.config.room.entities.climate.ShutterContactConfiguration;
import org.ambientlight.config.room.entities.climate.ThermostatConfiguration;
import org.ambientlight.messages.DispatcherType;
import org.ambientlight.messages.QeueManager;
import org.ambientlight.messages.max.DayEntry;
import org.ambientlight.messages.max.MaxDayInWeek;
import org.ambientlight.room.Room;
import org.ambientlight.room.entities.climate.devices.MaxComponent;
import org.ambientlight.room.entities.climate.devices.ShutterContact;
import org.ambientlight.room.entities.climate.devices.Thermostat;


/**
 * @author Florian Bornkessel
 * 
 */
public class ClimateFactory {

	public void initClimateManager(Room room, RoomConfiguration roomConfig, QeueManager queueManager) {

		// init ClimateManager
		if (roomConfig.climateManager != null) {

			List<String> invalidWeekProfiles = new ArrayList<String>();
			for (Entry<String, HashMap<MaxDayInWeek, List<DayEntry>>> currentWeekProfileEntrySet : room.config.climateManager.weekProfiles
					.entrySet()) {
				System.out.println("ClimateFactory initClimateManager(): parsing weekprofile: "
						+ currentWeekProfileEntrySet.getKey());
				boolean validProfile = this.parseWeekProfile(currentWeekProfileEntrySet.getValue());
				if (validProfile == false) {
					System.out.println(currentWeekProfileEntrySet.getKey() + " is invalid an will be skipped");
					invalidWeekProfiles.add(currentWeekProfileEntrySet.getKey());
				}
			}
			for (String currentWeekProfileToRemove : invalidWeekProfiles) {
				room.config.climateManager.weekProfiles.remove(currentWeekProfileToRemove);
			}

			room.setMaxComponents(new HashMap<Integer, MaxComponent>());
			room.climateManager = new ClimateManager();
			room.climateManager.config = room.config.climateManager;
			room.climateManager.queueManager = queueManager;
			room.qeueManager.registerMessageListener(DispatcherType.MAX, room.climateManager);

			for (MaxComponentConfiguration component : room.config.climateManager.devices.values()) {
				if (component instanceof ThermostatConfiguration) {
					Thermostat currentDevice = new Thermostat();
					currentDevice.config = (ThermostatConfiguration) component;
					room.getMaxComponents().put(currentDevice.config.adress, currentDevice);
					System.out.println("ClimateFactory initClimateManager(): added Thermostat: " + currentDevice.config.label);
				}
				if (component instanceof ShutterContactConfiguration) {
					ShutterContact currentDevice = new ShutterContact();
					currentDevice.config = (ShutterContactConfiguration) component;
					room.getMaxComponents().put(currentDevice.config.adress, currentDevice);
					System.out
					.println("ClimateFactory initClimateManager(): added ShutterContact: " + currentDevice.config.label);
				}
			}

			System.out.println("ClimateFactory initClimateManager(): initialized ClimateManager");
		}
	}


	public boolean parseWeekProfile(HashMap<MaxDayInWeek, List<DayEntry>> currentWeekProfile) {
		for (Entry<MaxDayInWeek, List<DayEntry>> currentDayEntry : currentWeekProfile.entrySet()) {

			if (currentDayEntry.getValue().size() > 13) {
				System.out.println("ClimateFactory - parseWeekProfile(): " + currentDayEntry.getKey()
						+ " has more than 13 entries");

				return false;
			}

			boolean validTerminationEntryFoun = false;
			for (DayEntry currentEntry : currentDayEntry.getValue()) {
				if (currentEntry.getHour() == 24 && currentEntry.getMin() == 0) {
					validTerminationEntryFoun = true;
					break;
				}
			}
			if (validTerminationEntryFoun == false) {
				System.out
				.println("ClimateFactory - parseWeekProfile(): " + currentDayEntry.getKey() + " has no Entry wit 24:00");
				return false;
			}

		}
		return true;
	}
}
