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

package org.ambient.util;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.room.entities.climate.ClimateImpl;
import org.ambientlight.room.entities.features.Entity;
import org.ambientlight.room.entities.features.climate.TemperaturMode;
import org.ambientlight.ws.Room;


/**
 * @author Florian Bornkessel
 * 
 */
public class RoomUtil {

	public static List<Entity> getEntities(final Room room) {

		ArrayList<Entity> result = new ArrayList<Entity>();

		if (room.lightObjectManager != null) {
			result.addAll(room.lightObjectManager.lightObjects.values());
		}

		if (room.switchesManager != null) {
			result.addAll(room.switchesManager.switches.values());
		}

		if (room.remoteSwitchesManager != null) {
			result.addAll(room.remoteSwitchesManager.remoteSwitches.values());
		}

		if (room.climateManager != null) {
			result.add(new ClimateImpl(new TemperaturMode(room.climateManager.temperature,
					room.climateManager.temporaryUntilDate, room.climateManager.mode)));
		}

		return result;
	}
}
