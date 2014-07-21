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
import org.ambientlight.messages.Message;
import org.ambientlight.messages.MessageListener;
import org.ambientlight.messages.QeueManager;
import org.ambientlight.messages.QeueManager.State;
import org.ambientlight.messages.max.MaxAckMessage;
import org.ambientlight.messages.max.MaxAckType;
import org.ambientlight.messages.max.MaxMessage;
import org.ambientlight.messages.max.MaxPairPingMessage;
import org.ambientlight.messages.max.MaxPairPongMessage;
import org.ambientlight.messages.max.MaxRegisterCorrelationMessage;
import org.ambientlight.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.messages.max.MaxShutterContactStateMessage;
import org.ambientlight.messages.max.MaxThermostatStateMessage;
import org.ambientlight.messages.max.MaxTimeInformationMessage;
import org.ambientlight.messages.max.MaxWakeUpMessage;
import org.ambientlight.room.entities.FeatureFacade;
import org.ambientlight.room.entities.climate.handlers.AddShutterContactHandler;
import org.ambientlight.room.entities.climate.handlers.AddThermostateHandler;
import org.ambientlight.room.entities.climate.handlers.MessageActionHandler;
import org.ambientlight.room.entities.climate.handlers.RemoveShutterContactHandler;
import org.ambientlight.room.entities.climate.handlers.RemoveThermostatHandler;
import org.ambientlight.room.entities.climate.util.MaxMessageCreator;
import org.ambientlight.room.entities.climate.util.MaxThermostateMode;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.climate.TemperaturMode;
import org.ambientlight.room.entities.features.sensor.TemperatureSensor;


/**
 * @author Florian Bornkessel
 * 
 */
public class ClimateManager extends Manager implements MessageListener, TemperatureSensor {

	public static int WAIT_FOR_NEW_DEVICES_TIMEOUT = 90;

	private CallBackManager callBackMananger;

	private QeueManager queueManager;

	private ClimateManagerConfiguration config;

	private List<MessageActionHandler> actionHandlers = new ArrayList<MessageActionHandler>();

	TimerTask syncTimeTask = new TimerTask() {

		@Override
		public void run() {
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

		// set time to thermostates at 3:00 every day
		Timer timer = new Timer();
		Calendar threePm = GregorianCalendar.getInstance();
		threePm.set(Calendar.HOUR_OF_DAY, 3);
		threePm.set(Calendar.MINUTE, 5);
		if (threePm.getTimeInMillis() < new Date().getTime()) {
			threePm.add(Calendar.DAY_OF_MONTH, 1);
		}
		timer.scheduleAtFixedRate(syncTimeTask, threePm.getTime(), 24 * 60 * 60 * 1000);

		// register sensors
		featureFacade.registerSensor(this);
		featureFacade.registerClimateManager(this);

		for (MaxComponent current : config.devices.values()) {
			if (current instanceof Thermostat) {
				featureFacade.registerSensor((TemperatureSensor) current);
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

			this.sendTimeInfoToThermostates();

			this.setMode(config.temperature, config.mode, config.temporaryUntilDate);
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
				device.timedOut = true;
				System.out.println("Climate Manager - onResponse(): Error! Timeout for Device: " + device.label);

			} else if (state == State.RETRIEVED_ANSWER && response instanceof MaxAckMessage
					&& ((MaxAckMessage) response).getAckType() == MaxAckType.ACK_INVALID_MESSAGE) {
				device.invalidArgument = true;
				System.out.println("Climate Manager - handleResponseMessage(): Device: Error! " + device.label
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
				System.out.println("ClimateManager handleMessage(): could not handle: " + message);
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
		config.temporaryUntilDate = message.getTemporaryUntil();

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
		if (shutterContact == null || message.getToAdress() != this.config.vCubeAdress) {
			System.out.println("ClimateManager handleShutterState(): got request from unknown device: adress="
					+ message.getFromAdress());
			return;
		}

		persistence.beginTransaction();

		ShutterContact shutter = (ShutterContact) config.devices.get(message.getFromAdress());
		shutter.batteryLow = message.isBatteryLow();
		shutter.isOpen = message.isOpen();
		shutter.rfError = message.hadRfError();
		shutter.lastUpdate = new Date(System.currentTimeMillis());

		persistence.commitTransaction();

		// inform thermostates
		boolean open = isAWindowOpen();
		sendWindowStateToThermostates(open);

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

		thermostat.batteryLow = message.isBatteryLow();
		thermostat.isLocked = message.isLocked();
		thermostat.lastUpdate = new Date(System.currentTimeMillis());

		thermostat.setTemperature(message.getActualTemp());

		if (message.getMode() == MaxThermostateMode.BOOST) {
			config.modeBeforeBoost = config.mode;
		}

		config.mode = message.getMode();
		config.temporaryUntilDate = message.getTemporaryUntil();
		config.temperature = message.getSetTemp();

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
		persistence.beginTransaction();

		if (config.weekProfiles.containsKey(profile) == false || config.weekProfiles.get(profile).isEmpty())
			throw new IllegalArgumentException("the selected weekProfile does not exist or is empty and unusable!");

		config.currentWeekProfile = profile;

		List<Message> messages = new ArrayList<Message>();

		for (MaxComponent current : config.devices.values()) {
			if (current instanceof Thermostat) {
				MaxWakeUpMessage wakeup = new MaxWakeUpMessage();
				wakeup.setFromAdress(config.vCubeAdress);
				wakeup.setToAdress(current.adress);
				wakeup.setSequenceNumber(new MaxMessageCreator(config).getNewSequnceNumber());
				messages.add(wakeup);

				messages.addAll(new MaxMessageCreator(config).getWeekProfileForDevice(current.adress, profile));
			}
		}

		queueManager.putOutMessages(messages);

		persistence.commitTransaction();

		// this.setMode(0.0f, MaxThermostateMode.AUTO, null);

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
			sendMessage.setToAdress(current.adress);
			sendMessage.setOpen(open);
			queueManager.putOutMessage(sendMessage);
		}
	}


	private void sendRegisterCorrelators() {
		List<Message> correlators = new ArrayList<Message>();
		for (MaxComponent currentDeviceConfig : config.devices.values()) {
			correlators
			.add(new MaxRegisterCorrelationMessage(DispatcherType.MAX, currentDeviceConfig.adress, config.vCubeAdress));
		}
		queueManager.putOutMessages(correlators);
	}


	private void sendTimeInfoToThermostates() {

		Date now = new Date();
		List<Message> messages = new ArrayList<Message>();

		for (MaxComponent current : config.devices.values()) {
			if (current instanceof Thermostat) {
				MaxTimeInformationMessage message = new MaxMessageCreator(config).getTimeInfoForDevice(now, current.adress);
				messages.add(message);
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
			if (current instanceof ShutterContact && ((ShutterContact) (current)).isOpen)
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


	public TemperaturMode getMode() {
		return new TemperaturMode(config.temperature, config.temporaryUntilDate, config.mode);
	}


	public void setMode(float temp, MaxThermostateMode mode, Date until) {
		persistence.beginTransaction();

		if (until != null && config.mode == MaxThermostateMode.TEMPORARY)
			throw new IllegalArgumentException("An until date may only be set in temporary mode.");

		// save state for restore
		if (mode == MaxThermostateMode.BOOST) {
			config.modeBeforeBoost = config.mode;
		}

		config.mode = mode;
		config.temperature = temp;
		config.temporaryUntilDate = until;

		List<Message> messages = new ArrayList<Message>();

		for (MaxComponent current : config.devices.values()) {
			if (current instanceof Thermostat) {
				MaxSetTemperatureMessage outMessage = new MaxMessageCreator(config).getSetTempForDevice(current.adress);
				messages.add(outMessage);
			}
		}

		queueManager.putOutMessages(messages);

		if (config.mode == MaxThermostateMode.AUTO) {
			Calendar now = GregorianCalendar.getInstance();
			MaxDayInWeek today = MaxDayInWeek.forCalendarDayInWeek(now.get(Calendar.DAY_OF_WEEK));
			List<DayEntry> entries = config.weekProfiles.get(config.currentWeekProfile).get(today);
			int nowInMinutesOfDay = now.get(Calendar.HOUR_OF_DAY) * 60;
			nowInMinutesOfDay += now.get(Calendar.MINUTE);
			int latestPossibleMins = 24 * 60;

			if (nowInMinutesOfDay == latestPossibleMins) {
				nowInMinutesOfDay = 0;
			}

			DayEntry nextDayEntry = null;
			for (DayEntry current : entries) {
				int currentMinutes = current.getHour() * 60 + current.getMin();
				if (currentMinutes > nowInMinutesOfDay && currentMinutes <= latestPossibleMins) {
					latestPossibleMins = currentMinutes;
					nextDayEntry = current;
				}
			}
			config.temperature = nextDayEntry.getTemp();
		}

		persistence.commitTransaction();
		callBackMananger.roomConfigurationChanged();
	}

}
