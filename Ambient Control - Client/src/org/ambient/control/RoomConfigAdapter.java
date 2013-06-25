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

package org.ambient.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ambientlight.room.RoomConfiguration;


/**
 * @author Florian Bornkessel
 *
 */
public class RoomConfigAdapter {

	public Map<String, RoomConfiguration> roomConfigurations = new HashMap<String, RoomConfiguration>();


	public RoomConfigurationParceable getRoomConfigAsParceable(String name) {
		return new RoomConfigurationParceable(roomConfigurations.get(name));
	}


	public ArrayList<String> getServerNames() {
		return new ArrayList<String>(roomConfigurations.keySet());
	}
}
