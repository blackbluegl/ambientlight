/*  Copyright 2013 Florian Bornkessel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.ambientlight.room.entities.climate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.ambientlight.Manager;
import org.ambientlight.Persistence;
import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.messages.DispatcherType;
import org.ambientlight.config.room.entities.climate.ClimateManagerConfiguration;
import org.ambientlight.config.room.entities.climate.DayEntry;
import org.ambientlight.config.room.entities.climate.MaxDayInWeek;
import org.ambientlight.config.room.entities.climate.TemperaturMode;
import org.ambientlight.rfmbridge.Message;
import org.ambientlight.rfmbridge.MessageListener;
import org.ambientlight.rfmbridge.QeueManager;
import org.ambientlight.rfmbridge.QeueManager.State;
import org.ambientlight.rfmbridge.messages.max.MaxAckMessage;
import org.ambientlight.rfmbridge.messages.max.MaxAckType;
import org.ambientlight.rfmbridge.messages.max.MaxConfigValveMessage;
import org.ambientlight.rfmbridge.messages.max.MaxConfigureTemperaturesMessage;
import org.ambientlight.rfmbridge.messages.max.MaxMessage;
import org.ambientlight.rfmbridge.messages.max.MaxPairPingMessage;
import org.ambientlight.rfmbridge.messages.max.MaxPairPongMessage;
import org.ambientlight.rfmbridge.messages.max.MaxRegisterCorrelationMessage;
import org.ambientlight.rfmbridge.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.rfmbridge.messages.max.MaxShutterContactStateMessage;
import org.ambientlight.rfmbridge.messages.max.MaxThermostatStateMessage;
import org.ambientlight.rfmbridge.messages.max.MaxTimeInformationMessage;
import org.ambientlight.rfmbridge.messages.max.MaxWakeUpMessage;
import org.ambientlight.room.entities.FeatureFacade;
import org.ambientlight.room.entities.climate.handlers.AddShutterContactHandler;
import org.ambientlight.room.entities.climate.handlers.AddThermostateHandler;
import org.ambientlight.room.entities.climate.handlers.MessageActionHandler;
import org.ambientlight.room.entities.climate.handlers.RemoveShutterContactHandler;
import org.ambientlight.room.entities.climate.handlers.RemoveThermostatHandler;
import org.ambientlight.room.entities.climate.util.MaxMessageCreator;
import org.ambientlight.room.entities.climate.util.MaxThermostateMode;
import org.ambientlight.room.entities.climate.util.MaxUtil;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.climate.Climate;
import org.ambientlight.room.entities.features.sensor.TemperatureSensor;


/**
 * @author Florian Bornkessel
 * 
 */
public class ClimateManager extends Manager implements MessageListener, TemperatureSensor {

	private long lastReconnect = 0;

	public static int WAIT_FOR_NEW_DEVICES_TIMEOUT = 180;

	private CallBackManager callBackMananger;

	private QeueManager queueManager;

	private ClimateManagerConfiguration config;

	private List<MessageActionHandler> actionHandlers = new ArrayList<MessageActionHandler>();

	TimerTask syncTimeTask = new TimerTask() {

		@Override
		public void run() {
			// wait a little time to let other climate manager complete their jobs
			int wait = (int) (Math.random() * 40.0f);
			try {
				Thread.sleep(wait * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			sendTimeInfoToThermostates();
		}
	};

	boolean learnMode = false;


	public ClimateManager(CallBackManager callBackMananger, QeueManager queueManager, ClimateManagerConfiguration config,
			FeatureFacade featureFacade, Persistence persistence) {
		super();
		this.callBackMananger = callBackMananger;
		this.queueManager = queueManager;
		this.config = config;
		this.persistence = persistence;

		// set time to thermostates at 3:00 every day beginning tomorrow
		Timer timer = new Timer();
		Calendar threePm = GregorianCalendar.getInstance();
		threePm.set(Calendar.HOUR_OF_DAY, 3);
		threePm.set(Calendar.MINUTE, 5);
		threePm.add(Calendar.DAY_OF_MONTH, 1);
		timer.scheduleAtFixedRate(syncTimeTask, threePm.getTime(), 24 * 60 * 60 * 1000);

		// register sensors
		featureFacade.registerSensor(this);
		featureFacade.registerClimateManager(this);
		if (config.devices != null) {
			for (MaxComponent current : config.devices.values()) {
				if (current instanceof Thermostat) {
					featureFacade.registerSensor((TemperatureSensor) current);
				}
			}
		}

		// reset boost mode if boost was finished before climate manager started up
		if (config.mode == MaxThermostateMode.BOOST) {
			if (config.boostUntil.before(new Date(System.currentTimeMillis()))) {
				config.mode = config.modeBeforeBoost;
			}
		}

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.MessageListener#onConnectionLost(org.ambientlight .messages.DispatcherType)
	 */
	@Override
	public void onDisconnectDispatcher(DispatcherType dispatcher) {

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.MessageListener#onConnectionRecovered(org. ambientlight.messages.DispatcherType)
	 */
	@Override
	public void onConnectDispatcher(DispatcherType dispatcher) {

		System.out.println("ClimateManager - onConnectDispatcher(): got connection. Syncing MAX devices.");
		if (dispatcher == DispatcherType.MAX) {

			this.sendRegisterCorrelators();

			if (System.currentTimeMillis() < lastReconnect + 3600 * 100000) {
				System.out
				.println("ClimateManager - onConnectDispatcher(): got connection. Last reconnect was earlier than one hour before. Do not sync data with MAX devices.");
				return;
			}

			// wait a little time to let other climate manager complete their jobs
			int wait = (int) (Math.random() * 40.0f);
			try {
				Thread.sleep(wait * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			this.sendWakeUpCallsToThermostates();
			this.sendValveConfigToThermostates();
			this.sendTempConfigToThermostates();
			this.sendTimeInfoToThermostates();
			this.setClimate(config.temperature, config.mode, config.temporaryUntil);
			this.lastReconnect = System.currentTimeMillis();
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.MessageListener#handleResponseMessages(org. ambientlight.messages.QeueManager.State,
	 * org.ambientlight.messages.Message, org.ambientlight.messages.Message)
	 */
	@Override
	public void onResponse(State state, Message response, Message request) {
		try {
			if (response != null) {
				System.out.println("ClimateManager - onResponse: called with response: " + response);
			}

			MaxComponent device = config.devices.get(((MaxMessage) request).getToAdress());

			if (device == null) {
				System.out.println("ClimateManager - onResponse: Device is unknown.");
				return;
			}

			// try to handle the message via an actionhandler
			for (MessageActionHandler current : actionHandlers) {
				if (current.onResponse(state, response, request)) {
					System.out.println("ClimateManager - onResponse: message handled by actionhandler.");
					return;
				}
			}

			// common handling of unhandled responses
			persistence.beginTransaction();

			if (state == State.TIMED_OUT) {
				device.setTimedOut(true);
				System.out.println("Climate Manager - onResponse(): Error! Timeout for Device: " + device.getLabel());

			} else if (state == State.RETRIEVED_ANSWER && response instanceof MaxAckMessage
					&& ((MaxAckMessage) response).getAckType() == MaxAckType.ACK_INVALID_MESSAGE) {
				device.setInvalidArgument(true);
				System.out.println("Climate Manager - handleResponseMessage(): Device: Error! " + device.getLabel()
						+ " reported invalid Arguments!");
			} else {
				System.out.println("Climate Manager - onResponse(): did not handle message");
			}

			persistence.commitTransaction();

		} catch (Exception e) {
			System.out.println("ClimateManager - onResponse: caught exception: ");
			e.printStackTrace();
			persistence.cancelTransaction();
		} finally {
			clearFinishedActionHandlers();

			callBackMananger.roomConfigurationChanged();
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.MessageListener#handleMessage(org.ambientlight .messages.Message)
	 */
	@Override
	public void onMessage(Message message) {

		try {

			// try to handle the message via an actionhandler
			for (MessageActionHandler current : actionHandlers) {
				if (current.onMessage(message)) {
					System.out.println("ClimateManager - onMessage: message handled by actionhandler.");
					return;
				}
			}

			// handle by ourself
			if (message instanceof MaxThermostatStateMessage) {
				System.out.println("ClimateManager - handleMessage(): handle " + message);
				handleThermostatState((MaxThermostatStateMessage) message);
			} else if (message instanceof MaxSetTemperatureMessage) {
				System.out.println("ClimateManager - handleMessage(): handle " + message);
				handleSetTemperature((MaxSetTemperatureMessage) message);
			} else if (message instanceof MaxShutterContactStateMessage) {
				System.out.println("ClimateManager - handleMessage(): handle " + message);
				handleShutterState((MaxShutterContactStateMessage) message);
			} else if (message instanceof MaxPairPingMessage) {
				System.out.println("ClimateManager - handleMessage(): handle " + message);
				handlePairPing((MaxPairPingMessage) message);
			} else if (message instanceof MaxTimeInformationMessage) {
				System.out.println("ClimateManager - handleMessage(): handle " + message);
				handleGetTimeInfo((MaxTimeInformationMessage) message);
			} else {
				System.out.println("ClimateManager handleMessage(): ignored message: " + message);
			}

		} catch (Exception e) {
			System.out.println("ClimateManager - onMessage: caught exception: ");
			e.printStackTrace();
		} finally {
			clearFinishedActionHandlers();
		}
	}


	/**
	 * @param message
	 */
	private void handleGetTimeInfo(MaxTimeInformationMessage message) {
		if (!message.isRequest() || config.devices.get(message.getFromAdress()) == null)
			return;
		System.out.println("ClimateManager - getTimeInfo: sending time to device: " + message.getFromAdress());
		MaxTimeInformationMessage time = new MaxMessageCreator(config).getTimeInfoForDevice(new Date(), message.getFromAdress());
		queueManager.putOutMessage(time);

	}


	/**
	 * @param message
	 * @throws IOException
	 *             public Map<String, Sensor> sensors;
	 */
	private void handleSetTemperature(MaxSetTemperatureMessage message) {

		Thermostat thermostat = (Thermostat) config.devices.get(message.getFromAdress());
		if (thermostat == null) {
			System.out.println("ClimateManager handleSetTemperature(): got request from unknown device: adress="
					+ message.getFromAdress());
			return;
		}

		persistence.beginTransaction();

		if (message.getMode() == MaxThermostateMode.BOOST) {
			config.modeBeforeBoost = config.mode;
		}
		config.mode = message.getMode();
		config.temperature = message.getTemp();
		config.temporaryUntil = message.getTemporaryUntil();

		persistence.commitTransaction();

		callBackMananger.roomConfigurationChanged();
	}


	/**
	 * @param message
	 * @throws IOException
	 */
	private void handlePairPing(MaxPairPingMessage message) {

		// known device wants to re-pair with some other device
		// todo check if thermostates want to refresh to shuttercontacts - we
		// have to handle this
		if (message.getToAdress().equals(config.vCubeAdress) == false && message.isReconnecting()) {
			System.out.println("ClimateManager handlePairPing(): Device wants to refresh pairing with some other device: "
					+ message);
		}

		// device wants to re-pair with vcube
		else if (message.isReconnecting() && message.getToAdress().equals(config.vCubeAdress) == true) {

			// device is known and will be refreshed
			if (config.devices.get(message.getFromAdress()) != null) {
				System.out.println("ClimateManager handlePairPing(): re-pairing device: " + message);

				MaxPairPongMessage pairPong = new MaxPairPongMessage();
				pairPong.setFromAdress(config.vCubeAdress);
				pairPong.setToAdress(message.getFromAdress());
				pairPong.setSequenceNumber(message.getSequenceNumber());
				queueManager.putOutMessage(pairPong);
			}
			// device is unknown
			else {
				System.out.println("ClimateManager handlePairPing(): Unknown Device wants to refresh pairing"
						+ " with us, but is unknwon. Ignoring: " + message);
			}
		}

		// create and add device in learnmode
		else if (learnMode && message.isReconnecting() == false) {
			switch (message.getDeviceType()) {
			case HEATING_THERMOSTAT:
				registerActionHandler(new AddThermostateHandler(message, callBackMananger, queueManager, persistence, config));
				break;
			case HEATING_THERMOSTAT_PLUS:
				registerActionHandler(new AddThermostateHandler(message, callBackMananger, queueManager, persistence, config));
				break;
			case SHUTTER_CONTACT:
				registerActionHandler(new AddShutterContactHandler(message, config, queueManager, callBackMananger, persistence));
				break;
			default:
				System.out.println("ClimateManager handlePairPing(): The devicetype is not supported yet: "
						+ message.getDeviceType());
				break;
			}
		}
	}


	/**
	 * @param message
	 * @throws IOException
	 */
	private void handleShutterState(MaxShutterContactStateMessage message) {

		ShutterContact shutterContact = (ShutterContact) config.devices.get(message.getFromAdress());
		if (shutterContact == null) {
			System.out.println("ClimateManager handleShutterState(): got request from unknown device: adress="
					+ message.getFromAdress());
			return;
		}

		persistence.beginTransaction();

		ShutterContact shutter = (ShutterContact) config.devices.get(message.getFromAdress());
		shutter.setBatteryLow(message.isBatteryLow());
		shutter.setOpen(message.isOpen());
		shutter.setRfError(message.hadRfError());
		shutter.setLastUpdate(new Date(System.currentTimeMillis()));

		persistence.commitTransaction();

		// inform thermostates
		sendWindowStateToThermostates(isAWindowOpen());

		callBackMananger.roomConfigurationChanged();
	}


	/**
	 * @param message
	 * @throws IOException
	 */
	private void handleThermostatState(MaxThermostatStateMessage message) {

		Thermostat thermostat = (Thermostat) config.devices.get(message.getFromAdress());
		if (thermostat == null) {
			System.out.println("ClimateManager handleThermostatState(): got request from unknown device: adress="
					+ message.getFromAdress());
			return;
		}

		persistence.beginTransaction();

		thermostat.setBatteryLow(message.isBatteryLow());
		thermostat.setLocked(message.isLocked());
		thermostat.setLastUpdate(new Date(System.currentTimeMillis()));
		thermostat.setTemperature(message.getActualTemp());
		thermostat.setRfError(message.hadRfError());

		config.mode = message.getMode();
		config.temporaryUntil = message.getTemporaryUntil();
		config.temperature = message.getSetTemp();
		if (message.getMode() == MaxThermostateMode.BOOST) {
			config.modeBeforeBoost = config.mode;
		}

		persistence.commitTransaction();

		callBackMananger.roomConfigurationChanged();
	}


	public void setFactoryResetDevice(int adress) throws IOException {

		MaxComponent device = config.devices.get(adress);
		if (device == null) {
			System.out.println("ClimateManager setFactoryResetDevice(): got request for unknown device: adress=" + adress);
			return;
		}

		if (device instanceof Thermostat) {
			RemoveThermostatHandler remove = new RemoveThermostatHandler((Thermostat) device, config.devices, callBackMananger,
					queueManager, persistence, config);
			this.actionHandlers.add(remove);
		}

		if (device instanceof ShutterContact) {
			RemoveShutterContactHandler remove = new RemoveShutterContactHandler(this, (ShutterContact) device, callBackMananger,
					queueManager, persistence, config);
			this.actionHandlers.add(remove);
		}

	}


	public void setCurrentProfile(String profile) {

		// validation that may harm an transaction
		if (config.weekProfiles.containsKey(profile) == false || config.weekProfiles.get(profile).isEmpty())
			throw new IllegalArgumentException("the selected weekProfile does not exist or is empty and unusable!");

		persistence.beginTransaction();

		config.currentWeekProfile = profile;
		// set temp according the profile day entry
		config.temperature = getCurrentTemperatureFromWeekProfile();

		persistence.commitTransaction();

		// send profile to thermostates
		List<Message> messages = new ArrayList<Message>();
		for (MaxComponent current : config.devices.values()) {
			if (current instanceof Thermostat) {
				// wakeup device - we will send more than one message
				MaxWakeUpMessage wakeup = new MaxWakeUpMessage();
				wakeup.setFromAdress(config.vCubeAdress);
				wakeup.setToAdress(current.getAdress());
				wakeup.setSequenceNumber(new MaxMessageCreator(config).getNewSequnceNumber());
				messages.add(wakeup);
				// send one or two messages per day for 7 days
				messages.addAll(new MaxMessageCreator(config).getWeekProfileForDevice(current.getAdress(), profile));
				// set the temperature that we have had updatet to the new actual temp in the week profile
				messages.add(new MaxMessageCreator(config).getSetTempForDevice(current.getAdress()));
			}
		}

		queueManager.putOutMessages(messages);

		callBackMananger.roomConfigurationChanged();
	}


	public void setPairingMode() {
		this.learnMode = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					System.out.println("ClimateManager - startPairing(): Waiting for new Devices");
					Thread.sleep(WAIT_FOR_NEW_DEVICES_TIMEOUT * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					learnMode = false;
					System.out.println("ClimateManager - startPairing(): Stopped waiting for new Devices");
				}
			}
		}).start();
	}


	public void sendWindowStateToThermostates(boolean open) {
		for (MaxComponent current : this.config.devices.values()) {
			if (current instanceof Thermostat == false) {
				continue;
			}
			MaxShutterContactStateMessage sendMessage = new MaxShutterContactStateMessage();
			sendMessage.setFromAdress(this.config.proxyShutterContactAdress);
			sendMessage.setFlags(0x6);
			sendMessage.setSequenceNumber(new MaxMessageCreator(config).getNewSequnceNumber());
			sendMessage.setToAdress(current.getAdress());
			sendMessage.setOpen(open);
			queueManager.putOutMessage(sendMessage);
		}
	}


	private void sendRegisterCorrelators() {
		if (config.devices == null)
			return;
		List<Message> correlators = new ArrayList<Message>();
		for (MaxComponent currentDeviceConfig : config.devices.values()) {
			correlators.add(new MaxRegisterCorrelationMessage(DispatcherType.MAX, currentDeviceConfig.getAdress(),
					config.vCubeAdress));
		}
		queueManager.putOutMessages(correlators);
	}


	private void sendTimeInfoToThermostates() {

		Date now = new Date();
		List<Message> messages = new ArrayList<Message>();

		for (MaxComponent current : config.devices.values()) {
			if (current instanceof Thermostat) {
				MaxTimeInformationMessage message = new MaxMessageCreator(config).getTimeInfoForDevice(now, current.getAdress());
				messages.add(message);
				System.out.println("Climate Manager - sendTimeInfoToThermostates(): sending time signal message:\n" + message);
			}
		}
		queueManager.putOutMessages(messages);
	}


	private void sendWakeUpCallsToThermostates() {

		List<Message> messages = new ArrayList<Message>();

		for (MaxComponent current : config.devices.values()) {
			if (current instanceof Thermostat) {

				MaxWakeUpMessage wakeup = new MaxWakeUpMessage();
				wakeup.setFromAdress(config.vCubeAdress);
				wakeup.setToAdress(current.getAdress());
				wakeup.setSequenceNumber(new MaxMessageCreator(config).getNewSequnceNumber());
				messages.add(wakeup);

				System.out.println("Climate Manager - sendWakeUpCalls(): sending wake up message:\n" + wakeup);
			}
		}
		queueManager.putOutMessages(messages);
	}


	private void sendValveConfigToThermostates() {

		List<Message> messages = new ArrayList<Message>();

		for (MaxComponent current : config.devices.values()) {
			if (current instanceof Thermostat) {

				MaxConfigValveMessage message = new MaxMessageCreator(config).getConfigValveForDevice(current.getAdress());
				messages.add(message);
				System.out
				.println("Climate Manager - sendValveConfigToThermostates(): sending valve config message:\n" + message);
			}
		}
		queueManager.putOutMessages(messages);
	}


	private void sendTempConfigToThermostates() {

		List<Message> messages = new ArrayList<Message>();

		for (MaxComponent current : config.devices.values()) {
			if (current instanceof Thermostat) {
				MaxConfigureTemperaturesMessage message = new MaxMessageCreator(config).getConfigureTemperatures(current
						.getAdress());
				messages.add(message);
				System.out.println("Climate Manager - sendTempConfigToThermostates(): sending valve config message:\n" + message);
			}
		}
		queueManager.putOutMessages(messages);
	}


	private void registerActionHandler(MessageActionHandler actionHandler) {
		if (actionHandler.isFinished())
			return;
		this.actionHandlers.add(actionHandler);
	}


	private void clearFinishedActionHandlers() {
		Iterator<MessageActionHandler> iterator = this.actionHandlers.iterator();
		while (iterator.hasNext()) {
			MessageActionHandler current = iterator.next();
			if (current.isFinished()) {
				System.out.println("Climatemanager - clearFinishedActionhandlers: removed Actionhandler: "
						+ current.getClass().getSimpleName());
				iterator.remove();
			}
		}
	}


	public boolean isAWindowOpen() {
		for (MaxComponent current : config.devices.values()) {
			if (current instanceof ShutterContact && ((ShutterContact) (current)).isOpen())
				return true;
		}
		return false;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.sensor.Sensor#getSensorId()
	 */
	@Override
	public EntityId getSensorId() {
		return new EntityId(EntityId.DOMAIN_TEMP_MAX, EntityId.ID_CLIMATE_MANAGER);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.sensor.TemperatureSensor# getTemperature()
	 */
	@Override
	public Object getSensorValue() {
		return this.config.temperature;
	}


	public Climate getClimate() {
		TemperaturMode mode = new TemperaturMode(config.temperature, config.temporaryUntil, config.mode);
		return new ClimateImpl(mode, config.modeBeforeBoost);
	}


	public void setBoostMode(boolean boost) {
		if (boost) {
			setClimate(config.temperature, MaxThermostateMode.BOOST, config.temporaryUntil);
		} else {
			setClimate(config.temperature, config.modeBeforeBoost, config.temporaryUntil);
		}
	}


	public void setClimate(float temp, MaxThermostateMode mode, Date until) {

		// validation that may harm an transaction
		if (until != null && config.mode == MaxThermostateMode.TEMPORARY)
			throw new IllegalArgumentException("An until date may only be set in temporary mode.");

		persistence.beginTransaction();

		// validate and correct temperatures
		if (temp < MaxUtil.MIN_TEMPERATURE) {
			temp = MaxUtil.MIN_TEMPERATURE;
		} else if (temp > MaxUtil.MAX_TEMPERATURE) {
			temp = MaxUtil.MAX_TEMPERATURE;
		}

		// handle boost params
		if (mode == MaxThermostateMode.BOOST) {
			// save state before boost for a restore if boost is switched off via setBoostMode()
			if (config.mode != MaxThermostateMode.BOOST) {
				config.modeBeforeBoost = config.mode;
			}
			Calendar boostUntil = GregorianCalendar.getInstance();
			boostUntil.add(Calendar.MINUTE, config.boostDurationMins);
			config.boostUntil = boostUntil.getTime();
		} else {
			config.modeBeforeBoost = mode;
		}

		// handle auto mode params
		if (mode == MaxThermostateMode.AUTO) {
			until = null;
			// if the minimum temperature is given we correct the value to actual from the current week profile
			if (temp <= MaxUtil.MIN_TEMPERATURE) {
				temp = getCurrentTemperatureFromWeekProfile();
			}
		}

		// handle maual mode
		if (mode == MaxThermostateMode.MANUAL) {
			until = null;
		}

		// common handling
		config.mode = mode;
		config.temperature = temp;
		config.temporaryUntil = until;

		// persist changes
		persistence.commitTransaction();

		// set temperatures to all thermostates
		List<Message> messages = new ArrayList<Message>();
		for (MaxComponent current : config.devices.values()) {
			if (current instanceof Thermostat) {
				MaxSetTemperatureMessage outMessage = new MaxMessageCreator(config).getSetTempForDevice(current.getAdress());
				messages.add(outMessage);
				System.out.println("Climate Manager - setClimate(): sending climate message:\n" + outMessage);
			}
		}
		queueManager.putOutMessages(messages);

		callBackMananger.roomConfigurationChanged();
	}


	/**
	 * @return
	 */
	private float getCurrentTemperatureFromWeekProfile() {
		float temp;
		Calendar now = GregorianCalendar.getInstance();
		int nowInMinutesOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

		// find the day entry that holds the current temperature
		MaxDayInWeek today = MaxDayInWeek.forCalendarDayInWeek(now.get(Calendar.DAY_OF_WEEK));
		List<DayEntry> entries = config.weekProfiles.get(config.currentWeekProfile).get(today);

		DayEntry nextDayEntry = new DayEntry(24, 0, 0);
		for (DayEntry current : entries) {
			int currentMinutes = current.getHour() * 60 + current.getMin();
			int nextDayEntryMinutes = nextDayEntry.getHour() * 60 + nextDayEntry.getMin();
			if (currentMinutes > nowInMinutesOfDay && currentMinutes <= nextDayEntryMinutes) {
				nextDayEntry = current;
			}
		}
		temp = nextDayEntry.getTemp();
		return temp;
	}

}
