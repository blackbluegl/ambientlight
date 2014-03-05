package org.ambientlight;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.room.Room;
import org.ambientlight.room.RoomFactory;


public class AmbientControlMW {

	static Map<String, Room> rooms = new HashMap<String, Room>();

	static RoomFactory roomFactory = new RoomFactory(new DeviceDriverFactory());


	/**
	 * init room for config.xml and return the roomName from the config
	 * 
	 * @param fileName
	 * @return configs room name
	 * @throws UnknownHostException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static String initRoom(String fileName) throws UnknownHostException, InterruptedException, IOException {

		Persistence persistence = new Persistence(fileName);

		RoomConfiguration roomConfiguration = persistence.getRoomConfiguration();

		Room room = roomFactory.initRoom(roomConfiguration, persistence);
		rooms.put(roomConfiguration.roomName, room);
		return roomConfiguration.roomName;
	}


	public static Room getRoom(String roomName) {
		return rooms.get(roomName);
	}


	/**
	 * @param currentRoom
	 */
	public static void destroyRoom(String currentRoom) {
		roomFactory.destroyRoom(rooms.get(currentRoom));
	}
}
