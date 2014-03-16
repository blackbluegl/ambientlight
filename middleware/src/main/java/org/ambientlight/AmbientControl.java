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

package org.ambientlight;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.room.Room;
import org.ambientlight.room.RoomFactory;


/**
 * @author Florian Bornkessel
 * 
 */
public class AmbientControl extends HttpServlet {

	private static final long serialVersionUID = 1L;

	static Map<String, Room> rooms = new HashMap<String, Room>();

	RoomFactory roomFactory = new RoomFactory(new DeviceDriverFactory());


	@Override
	public void init() throws ServletException {
		super.init();

		File configDir = new File(Persistence.DATA_DIRECTORY);
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		};
		String[] filenames = configDir.list(filter);
		for (String currentFileName : filenames) {
			try {

				Persistence persistence = new Persistence(currentFileName);

				RoomConfiguration roomConfiguration = persistence.getRoomConfiguration();

				Room room = roomFactory.initRoom(roomConfiguration, persistence);
				rooms.put(roomConfiguration.roomName, room);

			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}


	public static Room getRoom(String roomName) {
		return rooms.get(roomName);
	}


	public static Set<String> getRoomNames() {
		return rooms.keySet();
	}

	@Override
	public void destroy() {
		super.destroy();

		for (Room currentRoom : rooms.values()) {
			roomFactory.destroyRoom(currentRoom);
		}
	}
}
