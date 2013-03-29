package org.ambientlight;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Timer;

import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.scenery.entities.Room;
import org.ambientlight.scenery.entities.RoomFactory;
import org.ambientlight.scenery.entities.configuration.RoomConfiguration;
import org.ambientlight.scenery.rendering.Renderer;
import org.ambientlight.scenery.rendering.effects.RenderingEffectFactory;
import org.ambientlight.scenery.rendering.programms.RenderingProgrammFactory;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class AmbientLight {

	static RoomConfiguration roomConfig;

	static RenderingProgrammFactory renderProgrammFactory;
	
	static Renderer renderer;
	
	static Room room;
	
	static RoomFactory roomFactory;
	
	static final String BASE_URI = "http://ambientlight:9998/rest";
	//static final String BASE_URI = "http://localhost:9998/rest";

	int FREQUENCY = 25;
	
	static boolean debug = false;

	
	public void initSzene() throws InterruptedException, UnknownHostException,
			IOException {
		DeviceDriverFactory deviceFactory = new DeviceDriverFactory();
		roomFactory = new RoomFactory(deviceFactory);

		RoomConfiguration roomConfig = roomFactory
				.getRoomConfigByName("default");
		AmbientLight.setRoomConfig(roomConfig);

		try {
			room = roomFactory.loadRoom("default");
		} catch (Exception e) {
			System.out.println("error reading config file.");
			System.out.println(e.getMessage());
			System.exit(1);
		}

		RenderingEffectFactory effectFactory = new RenderingEffectFactory(room);
		RenderingProgrammFactory renderProgrammFactory = new RenderingProgrammFactory(
				effectFactory);
		
		renderer = new Renderer(room, renderProgrammFactory);
		
		AmbientLight.setRenderProgrammFactory(renderProgrammFactory);
		
		renderProgrammFactory.addAllLightObjectsInRoomToRenderer(renderer,
				room.getLightObjectsInRoom());

		Timer timer = new Timer();
		timer.schedule(new RenderingTask(), 0, 1000 / FREQUENCY);
		
	}

	public static void main(String[] args) throws InterruptedException,
			UnknownHostException, IOException {

		AmbientLight ambientLight = new AmbientLight();
		if(args.length>0){
			AmbientLight.debug=Boolean.parseBoolean(args[0]);
		}
		
		ambientLight.initSzene();
		
		try {
			// Start Webservice in seperate thread
			final ResourceConfig rc = new PackagesResourceConfig(
					"org.ambientlight.scenery.ws");
			rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
			rc.getFeatures().put("com.sun.jersey.config.feature.Trace", true);

			// HttpServer server = HttpServerFactory.create(BASE_URI, rc);

			GrizzlyServerFactory.createHttpServer(BASE_URI,rc);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static RoomFactory getRoomFactory() {
		return roomFactory;
	}

	public static void setRoomFactory(RoomFactory roomFactory) {
		AmbientLight.roomFactory = roomFactory;
	}

	public static Room getRoom() {
		return room;
	}

	public static void setRoom(Room room) {
		AmbientLight.room = room;
	}

	public static Renderer getRenderer() {
		return renderer;
	}

	public static void setRenderer(Renderer renderer) {
		AmbientLight.renderer = renderer;
	}

	public static RenderingProgrammFactory getRenderProgrammFactory() {
		return renderProgrammFactory;
	}

	public static void setRenderProgrammFactory(
			RenderingProgrammFactory renderProgrammFactory) {
		AmbientLight.renderProgrammFactory = renderProgrammFactory;
	}

	public static RoomConfiguration getRoomConfig() {
		return roomConfig;
	}

	public static void setRoomConfig(RoomConfiguration roomConfig) {
		AmbientLight.roomConfig = roomConfig;
	}


}



//final String MAIN_PACKAGE="org.ambientlight.scenery.ws";
//final String JERSEY_PACKAGE="com.sun.jersey.config.property.packages";
//
//    final Map<String, String> initParams = new HashMap<String, String>();      
//    initParams.put(JERSEY_PACKAGE, MAIN_PACKAGE);
//    initParams.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
//    System.out.println("Starting grizzly...");
//    SelectorThread threadSelector = GrizzlyWebContainerFactory.create(BASE_URI, initParams);
//
