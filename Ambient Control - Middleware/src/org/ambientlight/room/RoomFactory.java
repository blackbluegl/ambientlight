package org.ambientlight.room;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.device.drivers.DeviceConfiguration;
import org.ambientlight.config.events.DailyAlarmEvent;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.config.room.entities.alarm.AlarmManagerConfiguration;
import org.ambientlight.config.room.entities.lightobject.ActorConfiguration;
import org.ambientlight.config.room.entities.lightobject.LightObjectConfiguration;
import org.ambientlight.config.room.entities.scenery.SceneryManagerConfiguration;
import org.ambientlight.config.room.entities.switches.SwitchManagerConfiguration;
import org.ambientlight.config.room.triggers.EventGeneratorConfiguration;
import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.eventmanager.EventManager;
import org.ambientlight.messages.DispatcherManager;
import org.ambientlight.messages.QeueManager;
import org.ambientlight.process.ProcessFactory;
import org.ambientlight.room.entities.EventGenerator;
import org.ambientlight.room.entities.alarm.AlarmManager;
import org.ambientlight.room.entities.climate.ClimateFactory;
import org.ambientlight.room.entities.lightobject.LightObject;
import org.ambientlight.room.entities.sceneries.SceneryManager;
import org.ambientlight.room.entities.switches.SwitchManager;


public class RoomFactory {

	DeviceDriverFactory deviceFactory;
	ProcessFactory processFactory;


	public RoomFactory(DeviceDriverFactory deviceFactory, ProcessFactory processFactory) {
		this.deviceFactory = deviceFactory;
		this.processFactory = processFactory;
	}


	public Room initRoom(RoomConfiguration roomConfig) throws UnknownHostException, IOException {
		AmbientControlMW.setRoom(new Room());
		Room room = AmbientControlMW.getRoom();
		room.config = roomConfig;

		// start DispatcherManager
		DispatcherManager dispatcherManager = new DispatcherManager();

		// start queueManager
		room.qeueManager = new QeueManager();
		room.qeueManager.dispatcherManager = dispatcherManager;
		room.qeueManager.startQeues();

		// start climate Manager
		ClimateFactory climateFactory = new ClimateFactory();
		climateFactory.initClimateManager(room, roomConfig, room.qeueManager);

		// init CallbackManager
		CallBackManager callbackManager = new CallBackManager();
		room.callBackMananger = callbackManager;

		// initialize Pixelmap
		BufferedImage pixelMap = new BufferedImage(roomConfig.width, roomConfig.height, BufferedImage.TYPE_INT_ARGB);
		room.setRoomBitMap(pixelMap);

		// initialize the device drivers
		List<DeviceDriver> devices = new ArrayList<DeviceDriver>();
		for (DeviceConfiguration currentDeviceConfig : roomConfig.deviceConfigurations) {
			devices.add(this.initializeDevice(currentDeviceConfig, room));
		}
		room.setDevices(devices);

		// initialize the lightObjects
		List<LightObject> lightObjects = new ArrayList<LightObject>();
		for (ActorConfiguration currentItemConfiguration : roomConfig.lightObjectConfigurations.values()) {
			if (currentItemConfiguration instanceof LightObjectConfiguration) {
				lightObjects.add(this.initializeLightObject((LightObjectConfiguration) currentItemConfiguration,
						room.getAllStripePartsInRoom()));
			}
		}
		room.setLightObjectsInRoom(lightObjects);

		room.eventManager = new EventManager();

		createEventGenerators(room, room.eventManager);

		System.out.println("RoomFactory initRoom(): initialized ClimateManager");

		return room;
	}


	/**
	 * @param roomConfig
	 * @param room
	 */
	public void createEventGenerators(Room room, EventManager eventManager) {
		// initialize eventGenerators
		room.eventGenerators = new HashMap<String, EventGenerator>();

		for (EventGeneratorConfiguration currentConfig : room.config.eventGeneratorConfigurations.values()) {
			EventGenerator generator = null;
			if (currentConfig instanceof AlarmManagerConfiguration) {
				generator = new AlarmManager();
				AlarmManagerConfiguration alarmConfig = (AlarmManagerConfiguration) currentConfig;
				((AlarmManager) generator).createAlarm(new DailyAlarmEvent(alarmConfig.hour, alarmConfig.min, alarmConfig.name));
			}
			if (currentConfig instanceof SwitchManagerConfiguration) {
				generator = new SwitchManager();
			}
			if (currentConfig instanceof SceneryManagerConfiguration) {
				generator = new SceneryManager();
			}
			generator.config = currentConfig;
			generator.eventManager = eventManager;
			room.eventGenerators.put(generator.config.name, generator);
		}
	}


	private LightObject initializeLightObject(LightObjectConfiguration lightObjectConfig, List<StripePart> allStripePartsInRoom) {
		List<StripePart> stripePartsInLightObject = this.getStripePartsFromRoomForLightObject(allStripePartsInRoom,
				lightObjectConfig);

		return new LightObject(lightObjectConfig, stripePartsInLightObject);
	}


	private List<StripePart> getStripePartsFromRoomForLightObject(List<StripePart> stripesInRoom,
			LightObjectConfiguration configuration) {
		int minPositionX = configuration.xOffsetInRoom;
		int minPositionY = configuration.yOffsetInRoom;
		int maxPoistionX = configuration.xOffsetInRoom + configuration.width;
		int maxPositionY = configuration.yOffsetInRoom + configuration.height;

		List<StripePart> result = new ArrayList<StripePart>();

		for (StripePart currentSubStripe : stripesInRoom) {
			if (minPositionX > currentSubStripe.configuration.startXPositionInRoom) {
				continue;
			}
			if (minPositionY > currentSubStripe.configuration.startYPositionInRoom) {
				continue;
			}
			if (maxPoistionX < currentSubStripe.configuration.endXPositionInRoom) {
				continue;
			}
			if (maxPositionY < currentSubStripe.configuration.endYPositionInRoom) {
				continue;
			}
			result.add(currentSubStripe);
		}

		return result;
	}


	private DeviceDriver initializeDevice(DeviceConfiguration deviceConfig, Room room) throws UnknownHostException, IOException {

		DeviceDriver device = deviceFactory.createByName(deviceConfig, room);

		return device;
	}

}
