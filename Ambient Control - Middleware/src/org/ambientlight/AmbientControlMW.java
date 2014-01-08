package org.ambientlight;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Timer;

import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.process.ProcessFactory;
import org.ambientlight.room.Room;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.RoomFactory;
import org.ambientlight.room.entities.lightobject.LightObjectManager;
import org.ambientlight.room.entities.lightobject.Renderer;
import org.ambientlight.room.entities.lightobject.effects.RenderingEffectFactory;


public class AmbientControlMW {

	static String roomConfigFileName = "default";


	public static String getRoomConfigFileName() {
		return roomConfigFileName;
	}

	static LightObjectManager renderControl;

	static Renderer renderer;

	static Room room;

	static RoomFactory roomFactory;

	static ProcessFactory processFactory;

	public static final int FREQUENCY = 25;

	static boolean debug = false;

	static String bindingAdressAndPort = "0.0.0.0:9998";


	public static void main(String[] args) throws InterruptedException, UnknownHostException, IOException {

		parseArguments(args);

		RoomConfiguration roomConfiguration = initModel();

		initComponents(roomConfiguration);

		// start rendering but only if there is something to render
		if (roomConfiguration.lightObjectConfigurations.isEmpty() == false) {
			Timer timer = new Timer();
			timer.schedule(new RenderingTask(), 0, 1000 / FREQUENCY);
		} else {
			System.out.println("disabled the renderer because there are no lightObjects that need to be rendered");
		}

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

		processFactory = new ProcessFactory(room);
		processFactory.initProcesses();

		renderer = new Renderer(room);
		renderControl = new LightObjectManager(new RenderingEffectFactory(room));
	}


	private static RoomConfiguration initModel() {
		try {
			return RoomConfigurationFactory.getRoomConfigByName(roomConfigFileName);
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


	public static Renderer getRenderer() {
		return renderer;
	}


	public static void setRenderer(Renderer renderer) {
		AmbientControlMW.renderer = renderer;
	}


	public static LightObjectManager getRenderProgrammFactory() {
		return renderControl;
	}


	public static void setRenderProgrammFactory(LightObjectManager renderProgrammFactory) {
		AmbientControlMW.renderControl = renderProgrammFactory;
	}


	public static ProcessFactory getProcessFactory() {
		return processFactory;
	}
}
