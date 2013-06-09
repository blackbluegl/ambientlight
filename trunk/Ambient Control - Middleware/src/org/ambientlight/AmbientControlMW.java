package org.ambientlight;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Timer;

import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.actors.ActorConfiguration;
import org.ambientlight.room.actors.LightObjectConfiguration;
import org.ambientlight.room.entities.Room;
import org.ambientlight.room.entities.RoomConfigurationFactory;
import org.ambientlight.room.entities.RoomFactory;
import org.ambientlight.scenery.rendering.RenderControl;
import org.ambientlight.scenery.rendering.Renderer;
import org.ambientlight.scenery.rendering.effects.RenderingEffectFactory;
import org.ambientlight.scenery.ws.SceneryControl;

public class AmbientControlMW {

	static String roomConfigFileName = "default";

	public static String getRoomConfigFileName() {
		return roomConfigFileName;
	}

	static RoomConfiguration roomConfig;

	static RenderControl renderProgrammFactory;

	static Renderer renderer;

	static Room room;

	static RoomFactory roomFactory;

	static int FREQUENCY = 25;

	static boolean debug = false;

	static String bindingAdressAndPort = "0.0.0.0:9998";

	public static void main(String[] args) throws InterruptedException, UnknownHostException, IOException {

		parseArguments(args);

		initModel();

		initComponents();

		// start rendering but only if there is something to render
		if (doAnyLightObjectsExist(getRoomConfig())) {
			Timer timer = new Timer();
			timer.schedule(new RenderingTask(), 0, 1000 / FREQUENCY);
		}
		else{
			System.out.println("disabled the renderer because there are no lightObjects that need to be rendered");
		}

		//start scenery via SceneryControl
		SceneryControl sc = new SceneryControl();
		sc.changeRoomToScenery(getRoomConfig().currentScenery);

		new WebserviceTask().start();

		// wait for external signal to stop
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

	private static void initComponents() throws InterruptedException, UnknownHostException, IOException {
		DeviceDriverFactory deviceFactory = new DeviceDriverFactory();
		roomFactory = new RoomFactory(deviceFactory);

		room = roomFactory.initRoom(getRoomConfig());

		RenderingEffectFactory effectFactory = new RenderingEffectFactory(room);
		RenderControl renderProgrammFactory = new RenderControl(effectFactory);

		renderer = new Renderer(room, renderProgrammFactory);

		AmbientControlMW.setRenderProgrammFactory(renderProgrammFactory);
	}

	private static void initModel() {
		try {
			AmbientControlMW.setRoomConfig(RoomConfigurationFactory.getRoomConfigByName(roomConfigFileName));
		} catch (Exception e) {
			System.out.println("error reading config file.");
			System.out.println(e.getMessage());
			System.exit(1);
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
				if(currentArg.contains("config")){
					StringTokenizer st = new StringTokenizer(currentArg, "=");
					st.nextToken();
					AmbientControlMW.roomConfigFileName = st.nextToken();
				}
			}
		}
	}

	private static boolean doAnyLightObjectsExist(RoomConfiguration rc) {
		for (String currentActor : rc.actorConfigurations.keySet()) {
			ActorConfiguration current = rc.actorConfigurations.get(currentActor);
			if (current instanceof LightObjectConfiguration)
				return true;
		}
		return false;
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

	public static RenderControl getRenderProgrammFactory() {
		return renderProgrammFactory;
	}

	public static void setRenderProgrammFactory(RenderControl renderProgrammFactory) {
		AmbientControlMW.renderProgrammFactory = renderProgrammFactory;
	}

	public static RoomConfiguration getRoomConfig() {
		return roomConfig;
	}

	public static void setRoomConfig(RoomConfiguration roomConfig) {
		AmbientControlMW.roomConfig = roomConfig;
	}
}
