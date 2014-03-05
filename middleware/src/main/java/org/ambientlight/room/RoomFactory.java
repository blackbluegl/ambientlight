package org.ambientlight.room;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.ambientlight.Persistence;
import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.device.drivers.DeviceConfiguration;
import org.ambientlight.config.messages.DispatcherConfiguration;
import org.ambientlight.config.messages.DispatcherType;
import org.ambientlight.config.messages.QeueManagerConfiguration;
import org.ambientlight.config.process.ProcessManagerConfiguration;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.config.room.entities.alarm.AlarmManagerConfiguration;
import org.ambientlight.config.room.entities.climate.ClimateManagerConfiguration;
import org.ambientlight.config.room.entities.climate.DayEntry;
import org.ambientlight.config.room.entities.climate.MaxDayInWeek;
import org.ambientlight.config.room.entities.lightobject.LightObjectManagerConfiguration;
import org.ambientlight.config.room.entities.remoteswitches.RemoteSwitchManagerConfiguration;
import org.ambientlight.config.room.entities.switches.SwitchManagerConfiguration;
import org.ambientlight.device.drivers.AnimateableLedDevice;
import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.device.drivers.LedPointDeviceDriver;
import org.ambientlight.device.drivers.LedStripeDeviceDriver;
import org.ambientlight.device.drivers.RemoteSwtichDeviceDriver;
import org.ambientlight.device.led.LedPoint;
import org.ambientlight.device.led.Stripe;
import org.ambientlight.device.led.StripePart;
import org.ambientlight.events.EventManager;
import org.ambientlight.messages.Dispatcher;
import org.ambientlight.messages.DispatcherManager;
import org.ambientlight.messages.QeueManager;
import org.ambientlight.messages.max.MaxDispatcher;
import org.ambientlight.process.ProcessManager;
import org.ambientlight.room.entities.FeatureFacade;
import org.ambientlight.room.entities.alarm.AlarmManager;
import org.ambientlight.room.entities.climate.ClimateManager;
import org.ambientlight.room.entities.lightobject.LightObjectManager;
import org.ambientlight.room.entities.lightobject.Renderer;
import org.ambientlight.room.entities.lightobject.effects.RenderingEffectFactory;
import org.ambientlight.room.entities.remoteswitches.RemoteSwitchManager;
import org.ambientlight.room.entities.switches.SwitchManager;


public class RoomFactory {

	public final int FREQUENCY = 25;

	DeviceDriverFactory deviceFactory;


	public RoomFactory(DeviceDriverFactory deviceFactory) {
		this.deviceFactory = deviceFactory;
	}


	public void destroyRoom(Room room) {
		room.lightObjectManager.destroy();
	}


	public Room initRoom(RoomConfiguration roomConfig, Persistence persistence) {

		// init room
		Room room = new Room();
		room.config = roomConfig;

		// init CallbackManager
		room.callBackMananger = new CallBackManager(roomConfig.roomName, persistence);

		// init eventmanager
		room.eventManager = new EventManager();

		// init alarmManager
		room.alarmManager = initAlarmManager(roomConfig.alarmManager, room.eventManager, room.callBackMananger,
				room.featureFacade, persistence);

		// init remoteSwitchManager
		room.remoteSwitchManager = initRemoteSwitchManager(roomConfig.remoteSwitchesManager, room.callBackMananger,
				room.featureFacade, persistence);

		// init switchManager
		room.schwitchManager = initSwitchManager(roomConfig.switchesManager, room.eventManager, room.callBackMananger,
				room.featureFacade, persistence);

		// init lightObject rendering system
		room.lightObjectManager = initLightObjectManager(roomConfig.lightObjectManager, room.callBackMananger,
				room.featureFacade, persistence, roomConfig.debug);

		// init queueManager
		room.qeueManager = initQeueManager(roomConfig.qeueManager);

		// init climateManager
		room.climateManager = initClimateManager(roomConfig.climateManager, room.qeueManager, room.callBackMananger,
				room.featureFacade, persistence);

		// init EntitiesFacade
		room.featureFacade = new FeatureFacade(room.lightObjectManager);

		// init processManager
		room.processManager = initProcessManager(roomConfig.processManager, room.eventManager, persistence, room.featureFacade,
				room.callBackMananger);

		System.out.println("RoomFactory initRoom(): finished");

		return room;
	}


	/**
	 * @param processManager
	 * @param eventManager
	 * @param persistence
	 * @return
	 */
	private ProcessManager initProcessManager(ProcessManagerConfiguration config, EventManager eventManager,
			Persistence persistence, FeatureFacade featureFacade, CallBackManager callback) {

		if (config == null) {
			System.out.println("RoomFactory initProcessManager(): no configuration - skipping!");
			return null;
		}

		return new ProcessManager(config, eventManager, callback, featureFacade, persistence);
	}


	/**
	 * @param callBackMananger
	 * @param eventManager
	 * @param persistence
	 * @param switchesManager
	 * @return
	 */
	private SwitchManager initSwitchManager(SwitchManagerConfiguration config, EventManager eventManager,
			CallBackManager callBackMananger, FeatureFacade entitiesFacade, Persistence persistence) {

		if (config == null) {
			System.out.println("RoomFactory initSwitchManager(): no configuration - skipping!");
			return null;
		}

		return new SwitchManager(config, eventManager, callBackMananger, entitiesFacade, persistence);
	}


	/**
	 * @param persistence
	 * @param remoteSwitchesManager
	 * @return
	 */
	private RemoteSwitchManager initRemoteSwitchManager(RemoteSwitchManagerConfiguration config, CallBackManager callBackManager,
			FeatureFacade entitiesFacade, Persistence persistence) {

		if (config == null) {
			System.out.println("RoomFactory initRemoteSwitchManager(): no configuration - skipping!");
			return null;
		}

		RemoteSwtichDeviceDriver device = deviceFactory.createRemoteSwitchDevice(config.device);

		return new RemoteSwitchManager(config, device, callBackManager, entitiesFacade, persistence);
	}


	/**
	 * @param roomConfig
	 * @param room
	 */
	private AlarmManager initAlarmManager(AlarmManagerConfiguration config, EventManager eventManager,
			CallBackManager callBackManager, FeatureFacade entitiesFacade, Persistence persistence) {

		if (config == null) {
			System.out.println("RoomFactory initAlarmManager(): no configuration - skipping!");
			return null;
		}

		AlarmManager alarmManager = new AlarmManager(config, eventManager, callBackManager, entitiesFacade, persistence);

		return alarmManager;
	}


	private QeueManager initQeueManager(QeueManagerConfiguration config) {

		if (config == null) {
			System.out.println("RoomFactory initQeueManager(): no configuration - skipping!");
			return null;
		}

		QeueManager qeueManager = new QeueManager();

		HashMap<DispatcherType, Dispatcher> dispatcherModules = new HashMap<DispatcherType, Dispatcher>();
		for (DispatcherConfiguration dispatcherConfig : config.dispatchers) {
			if (dispatcherConfig.type.equals(DispatcherType.MAX)) {
				MaxDispatcher dispatcher = new MaxDispatcher(dispatcherConfig, qeueManager);
				dispatcherModules.put(DispatcherType.MAX, dispatcher);
				dispatcherModules.put(DispatcherType.SYSTEM, dispatcher);
			}
		}

		qeueManager.dispatcherManager = new DispatcherManager(qeueManager, dispatcherModules);

		qeueManager.startQeues();

		qeueManager.dispatcherManager.startDispatchers();

		return qeueManager;
	}


	/**
	 * @param config
	 * @param persistence
	 * @param room
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private LightObjectManager initLightObjectManager(LightObjectManagerConfiguration config, CallBackManager callBackManager,
			FeatureFacade entitiesFacade, Persistence persistence, boolean debug) {

		if (config == null || config.lightObjects.isEmpty()) {
			System.out.println("RoomFactory initLightObjectManager(): no configuration - skipping!");
			return null;
		}

		BufferedImage pixelMap = new BufferedImage(config.width, config.height, BufferedImage.TYPE_INT_ARGB);

		List<AnimateableLedDevice> ledDevices = new ArrayList<AnimateableLedDevice>();
		for (DeviceConfiguration currentDeviceConfig : config.devices) {
			ledDevices.add(deviceFactory.createLedDevice(currentDeviceConfig));
		}

		RenderingEffectFactory effectFactory = new RenderingEffectFactory(pixelMap);

		Renderer renderer = new Renderer(pixelMap, getAllStripePartsInRoom(ledDevices), getAllLedPointsInRoom(ledDevices));

		LightObjectManager manager = new LightObjectManager(pixelMap, config, effectFactory, ledDevices, renderer,
				callBackManager, entitiesFacade, persistence, FREQUENCY, debug);

		return manager;
	}


	public ClimateManager initClimateManager(ClimateManagerConfiguration config, QeueManager queueManager,
			CallBackManager callBackManager, FeatureFacade featurefacade, Persistence persistence) {

		if (config == null) {
			System.out.println("RoomFactory initClimateManager(): no configuration - skipping!");
			return null;
		}

		// parse weekProfiles
		List<String> invalidWeekProfiles = new ArrayList<String>();
		for (Entry<String, HashMap<MaxDayInWeek, List<DayEntry>>> currentWeekProfileEntrySet : config.weekProfiles.entrySet()) {

			System.out.println("ClimateFactory initClimateManager(): " + "parsing weekprofile: "
					+ currentWeekProfileEntrySet.getKey());

			boolean validProfile = this.parseWeekProfile(currentWeekProfileEntrySet.getValue());

			if (validProfile == false) {
				System.out.println(currentWeekProfileEntrySet.getKey() + " is invalid an will be skipped");
				invalidWeekProfiles.add(currentWeekProfileEntrySet.getKey());
			}
		}

		for (String currentWeekProfileToRemove : invalidWeekProfiles) {
			config.weekProfiles.remove(currentWeekProfileToRemove);
		}

		ClimateManager climateManager = new ClimateManager(callBackManager, queueManager, config, featurefacade, persistence);

		queueManager.registerMessageListener(DispatcherType.MAX, climateManager);

		System.out.println("ClimateFactory initClimateManager(): initialized ClimateManager");

		return climateManager;
	}


	private List<StripePart> getAllStripePartsInRoom(List<AnimateableLedDevice> devices) {
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


	private List<LedPoint> getAllLedPointsInRoom(List<AnimateableLedDevice> devices) {
		List<LedPoint> result = new ArrayList<LedPoint>();
		for (DeviceDriver currentDevice : devices) {
			if (currentDevice instanceof LedPointDeviceDriver) {
				LedPointDeviceDriver currentLedPointDevice = (LedPointDeviceDriver) currentDevice;
				result.addAll(currentLedPointDevice.getLedPoints());
			}
		}
		return result;
	}


	private boolean parseWeekProfile(HashMap<MaxDayInWeek, List<DayEntry>> currentWeekProfile) {
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
