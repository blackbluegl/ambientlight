package org.ambientlight;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Timer;

import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;
import org.ambientlight.room.objects.SwitchObjectConfiguration;
import org.ambientlight.scenery.entities.Room;
import org.ambientlight.scenery.entities.RoomFactory;
import org.ambientlight.scenery.rendering.Renderer;
import org.ambientlight.scenery.rendering.effects.RenderingEffectFactory;
import org.ambientlight.scenery.rendering.programms.RenderingProgrammFactory;

public class AmbientControlMW {

	static RoomConfiguration roomConfig;

	static RenderingProgrammFactory renderProgrammFactory;
	
	static Renderer renderer;
	
	static Room room;
	
	static RoomFactory roomFactory;

	static int FREQUENCY = 25;
	
	static boolean debug = false;
	
	static String bindingAdressAndPort = "0.0.0.0:9998";

	
	public void initSzene() throws InterruptedException, UnknownHostException,
			IOException {
		DeviceDriverFactory deviceFactory = new DeviceDriverFactory();
		roomFactory = new RoomFactory(deviceFactory);

		RoomConfiguration roomConfig = roomFactory
				.getRoomConfigByName("default");
		AmbientControlMW.setRoomConfig(roomConfig);

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
		
		AmbientControlMW.setRenderProgrammFactory(renderProgrammFactory);
		
		renderProgrammFactory.addAllLightObjectsInRoomToRenderer(renderer,
				room.getLightObjectsInRoom());

		//restore state of switchingUnits 
		for(RoomItemConfiguration currentItem: AmbientControlMW.getRoomConfig().roomItemConfigurations){
			if(currentItem instanceof SwitchObjectConfiguration){
				SwitchObjectConfiguration currentSwitch = (SwitchObjectConfiguration) currentItem;
			System.out.println("restoring switchingPowerState for: "+currentSwitch.name+"to: "+currentSwitch.getSceneryConfigurationBySceneryName(roomConfig.currentScenery).powerState);
			AmbientControlMW.getRoom().getSwitchingDevice().writeData(currentSwitch.deviceType, currentSwitch.houseCode, 
					currentSwitch.switchingUnitCode, currentSwitch.getSceneryConfigurationBySceneryName(roomConfig.currentScenery).powerState);
			}
		}
	}

	public static void main(String[] args) throws InterruptedException,
			UnknownHostException, IOException {

		AmbientControlMW ambientLight = new AmbientControlMW();
		
		//parse arguments
		if(args.length>0){
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
			}
		}
		
		//init scene and restoring switching units state
		ambientLight.initSzene();
		
		//start rendering - only if there is something todo
		if(AmbientControlMW.getRoomConfig().roomItemConfigurations.size()>0){
			Timer timer = new Timer();
			timer.schedule(new RenderingTask(), 0, 1000 / FREQUENCY);
		}
		
		//start Webservice in own thread
		WebserviceTask webservice = new WebserviceTask();
		webservice.start();
		
		// wait for external signal to stop
	        while (true) {
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException e){
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

	public static RenderingProgrammFactory getRenderProgrammFactory() {
		return renderProgrammFactory;
	}

	public static void setRenderProgrammFactory(
			RenderingProgrammFactory renderProgrammFactory) {
		AmbientControlMW.renderProgrammFactory = renderProgrammFactory;
	}

	public static RoomConfiguration getRoomConfig() {
		return roomConfig;
	}

	public static void setRoomConfig(RoomConfiguration roomConfig) {
		AmbientControlMW.roomConfig = roomConfig;
	}
}
