package org.ambientlight;

import java.io.IOException;
import java.net.UnknownHostException;

import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.room.Room;
import org.ambientlight.room.RoomFactory;


public class AmbientControlMW {

	static Room room;

	static RoomFactory roomFactory;


	public static void init() throws UnknownHostException, InterruptedException, IOException {


		RoomConfiguration roomConfiguration = getModelFromFile();

		initComponents(roomConfiguration);
	}


	private static void initComponents(RoomConfiguration roomConfiguration) throws InterruptedException, UnknownHostException,
	IOException {
		roomFactory = new RoomFactory(new DeviceDriverFactory());
		room = roomFactory.initRoom(roomConfiguration);
	}


	private static RoomConfiguration getModelFromFile(String fileName) {
		try {
			return Persistence.getRoomConfigByName(fileName);
		} catch (Exception e) {
			System.out.println("error reading config file.");
			System.out.println(e.getMessage());
			return null;
		}
	}


	public static RoomFactory getRoomFactory() {
		return roomFactory;
	}


	public static Room getRoom(String roomName) {
		return room;
	}


	public static void setRoom(Room room) {
		AmbientControlMW.room = room;
	}

}
