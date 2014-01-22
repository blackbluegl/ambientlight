package org.ambientlight.room;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.device.drivers.DeviceConfiguration;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.config.room.entities.alarm.AlarmManagerConfiguration;
import org.ambientlight.config.room.entities.lightobject.LightObjectManagerConfiguration;
import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.device.drivers.LedPointDeviceDriver;
import org.ambientlight.device.drivers.LedStripeDeviceDriver;
import org.ambientlight.device.led.LedPoint;
import org.ambientlight.device.led.Stripe;
import org.ambientlight.device.led.StripePart;
import org.ambientlight.eventmanager.EventManager;
import org.ambientlight.messages.Dispatcher;
import org.ambientlight.messages.DispatcherConfiguration;
import org.ambientlight.messages.DispatcherManager;
import org.ambientlight.messages.DispatcherType;
import org.ambientlight.messages.QeueManager;
import org.ambientlight.messages.max.DayEntry;
import org.ambientlight.messages.max.MaxDayInWeek;
import org.ambientlight.messages.max.MaxDispatcher;
import org.ambientlight.process.ProcessManager;
import org.ambientlight.room.entities.alarm.AlarmManager;
import org.ambientlight.room.entities.climate.ClimateManager;
import org.ambientlight.room.entities.lightobject.LightObjectManager;
import org.ambientlight.room.entities.lightobject.Renderer;
import org.ambientlight.room.entities.lightobject.effects.RenderingEffectFactory;


public class RoomFactory {

	DeviceDriverFactory deviceFactory;


	public RoomFactory(DeviceDriverFactory deviceFactory, ProcessManager processFactory) {
		this.deviceFactory = deviceFactory;
	}


	public Room initRoom(RoomConfiguration roomConfig) throws UnknownHostException, IOException {

		// init room
		Room room = new Room();
		room.config = roomConfig;
		AmbientControlMW.setRoom(room);

		// init eventmanager
		room.eventManager = new EventManager();

		// init AlarmManager
		room.alarmManager = initAlarmManager(roomConfig.alarmManager, room.eventManager);

		// init remoteSwitchManager

		// init switchManager

		// init lightObject rendering system
		room.lightObjectManager = initLightObjectManager(roomConfig.lightObjectManager);

		// TODO init wie alarmManager bei allen weiteren
		// init queueManager
		initQeueManager(roomConfig, room);

		// init climate Manager
		initClimateManager(room, roomConfig, room.qeueManager);

		// init CallbackManager
		room.callBackMananger = new CallBackManager();

		System.out.println("RoomFactory initRoom(): finished");

		return room;
	}


	/**
	 * @param roomConfig
	 * @param room
	 */
	private AlarmManager initAlarmManager(AlarmManagerConfiguration config, EventManager eventManager) {
		if (config == null) {
			System.out.println("RoomFactory initAlarmManager(): no configuration - skipping!");
		}

		AlarmManager alarmManager = new AlarmManager(config, eventManager);

		return alarmManager;
	}


	/**
	 * @param roomConfig
	 * @param room
	 */
	private void initQeueManager(RoomConfiguration roomConfig, Room room) {
		room.qeueManager = new QeueManager();

		HashMap<DispatcherType, Dispatcher> dispatcherModules = new HashMap<DispatcherType, Dispatcher>();
		for (DispatcherConfiguration dispatcherConfig : roomConfig.qeueManager.dispatchers) {
			if (dispatcherConfig.type.equals(DispatcherType.MAX)) {
				MaxDispatcher dispatcher = new MaxDispatcher(dispatcherConfig, room.qeueManager);
				dispatcherModules.put(DispatcherType.MAX, dispatcher);
				dispatcherModules.put(DispatcherType.SYSTEM, dispatcher);
			}
		}

		room.qeueManager.dispatcherManager = new DispatcherManager(room.qeueManager, dispatcherModules);

		room.qeueManager.startQeues();

		room.qeueManager.dispatcherManager.startDispatchers();
	}


	/**
	 * @param roomConfig
	 * @param room
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private LightObjectManager initLightObjectManager(LightObjectManagerConfiguration roomConfig) throws UnknownHostException,
			IOException {
		if (roomConfig == null) {
			System.out.println("RoomFactory initLightObjectManager(): no configuration - skipping!");
		}

		BufferedImage pixelMap = new BufferedImage(roomConfig.width, roomConfig.height, BufferedImage.TYPE_INT_ARGB);

		List<DeviceDriver> ledDevices = new ArrayList<DeviceDriver>();
		for (DeviceConfiguration currentDeviceConfig : roomConfig.lightObjectManager.deviceConfigurations) {
			ledDevices.add(deviceFactory.createByName(currentDeviceConfig));
		}

		RenderingEffectFactory effectFactory = new RenderingEffectFactory(pixelMap);

		Renderer renderer = new Renderer(pixelMap, getAllStripePartsInRoom(ledDevices), getAllLedPointsInRoom(ledDevices));

		return new LightObjectManager(pixelMap, roomConfig.lightObjectManager, effectFactory, ledDevices,
				renderer);
	}


	private List<StripePart> getAllStripePartsInRoom(List<DeviceDriver> devices) {
		List<StripePart> result = new ArrayList<StripePart>();
		for (DeviceDriver currentDevice : devices) {
			if (currentDevice instanceof LedStripeDeviceDriver) {
				LedStripeDeviceDriver currentLedStripeDevice = (LedStripeDeviceDriver) currentDevice;
				for (Stripe currentStripe : currentLedStripeDevice.getAllStripes()) {
					result.addAll(currentStripe.getStripeParts());
				}
			}
		}
		return result;
	}


	private List<LedPoint> getAllLedPointsInRoom(List<DeviceDriver> devices) {
		List<LedPoint> result = new ArrayList<LedPoint>();
		for (DeviceDriver currentDevice : devices) {
			if (currentDevice instanceof LedPointDeviceDriver) {
				LedPointDeviceDriver currentLedPointDevice = (LedPointDeviceDriver) currentDevice;
				result.addAll(currentLedPointDevice.getLedPoints());
			}
		}
		return result;
	}


	public void initClimateManager(Room room, RoomConfiguration roomConfig, QeueManager queueManager) {

		// init ClimateManager
		if (roomConfig.climateManager != null) {

			List<String> invalidWeekProfiles = new ArrayList<String>();
			for (Entry<String, HashMap<MaxDayInWeek, List<DayEntry>>> currentWeekProfileEntrySet : room.config.climateManager.weekProfiles
					.entrySet()) {
				System.out.println("ClimateFactory initClimateManager(): parsing weekprofile: "
						+ currentWeekProfileEntrySet.getKey());
				boolean validProfile = this.parseWeekProfile(currentWeekProfileEntrySet.getValue());
				if (validProfile == false) {
					System.out.println(currentWeekProfileEntrySet.getKey() + " is invalid an will be skipped");
					invalidWeekProfiles.add(currentWeekProfileEntrySet.getKey());
				}
			}
			for (String currentWeekProfileToRemove : invalidWeekProfiles) {
				room.config.climateManager.weekProfiles.remove(currentWeekProfileToRemove);
			}

			room.climateManager = new ClimateManager();
			room.climateManager.config = room.config.climateManager;
			room.climateManager.queueManager = queueManager;
			room.qeueManager.registerMessageListener(DispatcherType.MAX, room.climateManager);

			System.out.println("ClimateFactory initClimateManager(): initialized ClimateManager");
		}
	}


	public boolean parseWeekProfile(HashMap<MaxDayInWeek, List<DayEntry>> currentWeekProfile) {
		for (Entry<MaxDayInWeek, List<DayEntry>> currentDayEntry : currentWeekProfile.entrySet()) {

			if (currentDayEntry.getValue().size() > 13) {
				System.out.println("ClimateFactory - parseWeekProfile(): " + currentDayEntry.getKey()
						+ " has more than 13 entries");

				return false;
			}

			boolean validTerminationEntryFoun = false;
			for (DayEntry currentEntry : currentDayEntry.getValue()) {
				if (currentEntry.getHour() == 24 && currentEntry.getMin() == 0) {
					validTerminationEntryFoun = true;
					break;
				}
			}
			if (validTerminationEntryFoun == false) {
				System.out
				.println("ClimateFactory - parseWeekProfile(): " + currentDayEntry.getKey() + " has no Entry wit 24:00");
				return false;
			}

		}
		return true;
	}

}
