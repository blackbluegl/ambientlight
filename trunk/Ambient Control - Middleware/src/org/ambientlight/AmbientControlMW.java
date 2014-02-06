package org.ambientlight;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.process.ProcessManager;
import org.ambientlight.room.Persistence;
import org.ambientlight.room.Room;
import org.ambientlight.room.RoomFactory;


public class AmbientControlMW {

	static String roomConfigFileName = "default";




	static Room room;

	static RoomFactory roomFactory;

	static ProcessManager processFactory;

	static boolean debug = false;

	static String bindingAdressAndPort = "0.0.0.0:9998";


	public static void main(String[] args) throws InterruptedException, UnknownHostException, IOException {

		parseArguments(args);

		RoomConfiguration roomConfiguration = getModelFromFile();

		initComponents(roomConfiguration);

		new WebserviceTask().start();

		// wait for external signal to stop
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}


	private static void initComponents(RoomConfiguration roomConfiguration) throws InterruptedException, UnknownHostException,
	IOException {

		DeviceDriverFactory deviceFactory = new DeviceDriverFactory();

		roomFactory = new RoomFactory(deviceFactory, processFactory);

		roomFactory.initRoom(roomConfiguration);
		// TODO extract to RoomFactory and Room
		processFactory = new ProcessManager(room);
		processFactory.initProcesses();

	}


	private static RoomConfiguration getModelFromFile() {
		try {
			return Persistence.getRoomConfigByName(roomConfigFileName);
		} catch (Exception e) {
			System.out.println("error reading config file.");
			System.out.println(e.getMessage());
			return null;
		}
	}


	private static void parseArguments(String[] args) {
		// parse arguments
		if (args.length > 0) {
			for (String currentArg : args) {
				if (currentArg.contains("debug")) {
					StringTokenizer st = new StringTokenizer(currentArg, "=");
					st.nextToken();
					AmbientControlMW.debug = Boolean.parseBoolean(st.nextToken());
				}
				if (currentArg.contains("binding")) {
					StringTokenizer st = new StringTokenizer(currentArg, "=");
					st.nextToken();
					AmbientControlMW.bindingAdressAndPort = st.nextToken();
				}
				if (currentArg.contains("config")) {
					StringTokenizer st = new StringTokenizer(currentArg, "=");
					st.nextToken();
					AmbientControlMW.roomConfigFileName = st.nextToken();
				}
			}
		}
	}


	public static RoomFactory getRoomFactory() {
		return roomFactory;
	}


	public static void setRoomFactory(RoomFactory roomFactory) {
		AmbientControlMW.roomFactory = roomFactory;
	}


	public static Room getRoom() {
		return room;
	}


	public static void setRoom(Room room) {
		AmbientControlMW.room = room;
	}


	public static ProcessManager getProcessFactory() {
		return processFactory;
	}


	public static String getRoomConfigFileName() {
		return roomConfigFileName;
	}


	public static boolean isDebug() {
		return debug;
	}
}
