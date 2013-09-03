package org.ambientlight.room;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ambientlight.device.drivers.DeviceConfiguration;
import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.process.ProcessFactory;
import org.ambientlight.process.eventmanager.EventManager;
import org.ambientlight.room.actors.ActorConfiguration;
import org.ambientlight.room.actors.LightObjectConfiguration;
import org.ambientlight.room.entities.AlarmGenerator;
import org.ambientlight.room.entities.EventGenerator;
import org.ambientlight.room.entities.LightObject;
import org.ambientlight.room.entities.SceneryEventGenerator;
import org.ambientlight.room.entities.Sensor;
import org.ambientlight.room.entities.SwitchEventGenerator;
import org.ambientlight.room.eventgenerator.AlarmEventGeneratorConfiguration;
import org.ambientlight.room.eventgenerator.EventGeneratorConfiguration;
import org.ambientlight.room.eventgenerator.SceneryEventGeneratorConfiguration;
import org.ambientlight.room.eventgenerator.SwitchEventGeneratorConfiguration;


public class RoomFactory {

	DeviceDriverFactory deviceFactory;
	ProcessFactory processFactory;


	public RoomFactory(DeviceDriverFactory deviceFactory, ProcessFactory processFactory) {
		this.deviceFactory = deviceFactory;
		this.processFactory = processFactory;
	}


	public Room initRoom(RoomConfiguration roomConfig) throws UnknownHostException, IOException {
		Room room = new Room();

		room.config = roomConfig;

		// initialize Pixelmap
		BufferedImage pixelMap = new BufferedImage(roomConfig.width, roomConfig.height, BufferedImage.TYPE_INT_ARGB);
		room.setRoomBitMap(pixelMap);

		// initialize the device drivers
		List<DeviceDriver> devices = new ArrayList<DeviceDriver>();
		for (DeviceConfiguration currentDeviceConfig : roomConfig.deviceConfigurations) {
			devices.add(this.initializeDevice(currentDeviceConfig));
		}
		room.setDevices(devices);

		// init sensors
		room.sensors = new HashMap<String, Sensor>();

		// initialize the lightObjects
		List<LightObject> lightObjects = new ArrayList<LightObject>();
		for (ActorConfiguration currentItemConfiguration : roomConfig.actorConfigurations.values()) {
			if (currentItemConfiguration instanceof LightObjectConfiguration) {
				lightObjects.add(this.initializeLightObject((LightObjectConfiguration) currentItemConfiguration,
						room.getAllStripePartsInRoom()));
			}
		}
		room.setLightObjectsInRoom(lightObjects);

		room.eventManager = new EventManager();

		createEventGenerators(room, room.eventManager);

		return room;
	}


	/**
	 * @param roomConfig
	 * @param room
	 */
	public void createEventGenerators(Room room, EventManager eventManager) {
		// initialize eventGenerators
		room.eventGenerators = new HashMap<String, EventGenerator>();

		for (EventGeneratorConfiguration currentConfig : room.config.eventGeneratorConfigurations) {
			EventGenerator generator = null;
			if (currentConfig instanceof AlarmEventGeneratorConfiguration) {
				generator = new AlarmGenerator();
			}
			if (currentConfig instanceof SwitchEventGeneratorConfiguration) {
				generator = new SwitchEventGenerator();
			}
			if (currentConfig instanceof SceneryEventGeneratorConfiguration) {
				generator = new SceneryEventGenerator();
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


	private DeviceDriver initializeDevice(DeviceConfiguration deviceConfig) throws UnknownHostException, IOException {

		DeviceDriver device = deviceFactory.createByName(deviceConfig);

		return device;
	}

}
