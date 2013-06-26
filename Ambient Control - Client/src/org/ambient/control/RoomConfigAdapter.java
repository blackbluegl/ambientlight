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
import java.util.List;
import java.util.Map;

import org.ambientlight.room.RoomConfiguration;


/**
 * @author Florian Bornkessel
 * 
 */
public class RoomConfigAdapter {

	Map<String, RoomConfigurationUpdateListener> listeners = new HashMap<String, RoomConfigurationUpdateListener>();
	List<RoomConfigurationUpdateListener> metaListeners = new ArrayList<RoomConfigurationUpdateListener>();

	public interface RoomConfigurationUpdateListener {
		public void onRoomConfigurationChange(String serverName, RoomConfiguration config);
	}


	private final Map<String, RoomConfiguration> roomConfigurations = new HashMap<String, RoomConfiguration>();


	public Map<String, RoomConfiguration> getAllRoomConfigurations() {
		return this.roomConfigurations;
	}

	public void addMetaListener(RoomConfigurationUpdateListener listener) {
		this.metaListeners.add(listener);
	}


	public RoomConfiguration getRoomConfiguration(String serverName) {
		return this.roomConfigurations.get(serverName);
	}

	public void addRoomConfiguration(String server, RoomConfiguration config) {
		this.roomConfigurations.put(server, config);
	}


	public void updateRoomConfiguration(String server, RoomConfiguration config) {
		this.roomConfigurations.put(server, config);

		for (RoomConfigurationUpdateListener metaListener : metaListeners) {
			metaListener.onRoomConfigurationChange(server, config);
		}

		this.listeners.get(server).onRoomConfigurationChange(server, config);

	}


	public void addRoomConfigurationChangeListener(String server, RoomConfigurationUpdateListener listener) {
		this.listeners.put(server, listener);
	}


	public void removeRoomConfigurationChangeListener(String server, RoomConfigurationUpdateListener listener) {
		this.listeners.remove(server);
	}


	public RoomConfigurationParceable getRoomConfigAsParceable(String serverName) {
		return new RoomConfigurationParceable(roomConfigurations.get(serverName));
	}


	public ArrayList<String> getServerNames() {
		return new ArrayList<String>(roomConfigurations.keySet());
	}
}

