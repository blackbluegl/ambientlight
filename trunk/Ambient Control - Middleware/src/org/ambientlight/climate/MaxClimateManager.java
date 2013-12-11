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

package org.ambientlight.climate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.room.ClimateConfiguration;
import org.ambientlight.config.room.actors.MaxComponentConfiguration;
import org.ambientlight.config.room.actors.ShutterContactConfiguration;
import org.ambientlight.config.room.actors.ThermostatConfiguration;
import org.ambientlight.messages.ConditionalMessage;
import org.ambientlight.messages.DispatcherType;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.MessageListener;
import org.ambientlight.messages.QeueManager;
import org.ambientlight.messages.QeueManager.State;
import org.ambientlight.messages.max.DayEntry;
import org.ambientlight.messages.max.DeviceType;
import org.ambientlight.messages.max.MaxAckMessage;
import org.ambientlight.messages.max.MaxAckType;
import org.ambientlight.messages.max.MaxAddLinkPartnerMessage;
import org.ambientlight.messages.max.MaxConfigValveMessage;
import org.ambientlight.messages.max.MaxConfigValveMessage.DecalcEntry;
import org.ambientlight.messages.max.MaxConfigureTemperaturesMessage;
import org.ambientlight.messages.max.MaxConfigureWeekProgrammMessage;
import org.ambientlight.messages.max.MaxDayInWeek;
import org.ambientlight.messages.max.MaxFactoryResetMessage;
import org.ambientlight.messages.max.MaxMessage;
import org.ambientlight.messages.max.MaxPairPingMessage;
import org.ambientlight.messages.max.MaxPairPongMessage;
import org.ambientlight.messages.max.MaxRegisterCorrelationMessage;
import org.ambientlight.messages.max.MaxRemoveLinkPartnerMessage;
import org.ambientlight.messages.max.MaxSetGroupIdMessage;
import org.ambientlight.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.messages.max.MaxShutterContactStateMessage;
import org.ambientlight.messages.max.MaxThermostatStateMessage;
import org.ambientlight.messages.max.MaxThermostateMode;
import org.ambientlight.messages.max.MaxTimeInformationMessage;
import org.ambientlight.messages.max.MaxUnregisterCorrelationMessage;
import org.ambientlight.messages.max.MaxWakeUpMessage;
import org.ambientlight.messages.max.WaitForShutterContactCondition;
import org.ambientlight.messages.rfm22bridge.UnRegisterCorrelatorMessage;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.entities.MaxComponent;
import org.ambientlight.room.entities.ShutterContact;
import org.ambientlight.room.entities.Thermostat;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxClimateManager implements MessageListener {

	public QeueManager queueManager;

	TimerTask syncTimeTask = new TimerTask() {

		@Override
		public void run() {
			sendTimeInfoToComponents();
		}
	};

	public ClimateConfiguration config;

	private int outSequenceNumber = 0;

	boolean learnMode = false;
	public static int WAIT_FOR_NEW_DEVICES = 90;


	public MaxClimateManager() {
		Timer timer = new Timer();
		Calendar threePm = GregorianCalendar.getInstance();
		threePm.set(Calendar.HOUR_OF_DAY, 3);
		threePm.set(Calendar.MINUTE, 5);
		if (threePm.getTimeInMillis() < new Date().getTime()) {
			threePm.add(Calendar.DAY_OF_MONTH, 1);
		}

		timer.scheduleAtFixedRate(syncTimeTask, threePm.getTime(), 24 * 60 * 60 * 1000);
	}


	/**
	 * 
	 */
	private void sendRegisterCorrelators() {
		List<Message> correlators = new ArrayList<Message>();
		for (MaxComponentConfiguration currentDeviceConfig : config.devices.values()) {
			correlators
			.add(new MaxRegisterCorrelationMessage(DispatcherType.MAX, currentDeviceConfig.adress, config.vCubeAdress));
		}
		queueManager.putOutMessages(correlators);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.messages.MessageListener#handleMessage(org.ambientlight
	 * .messages.Message)
	 */
	@Override
	public void onMessage(Message message) {
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
			System.out.println("ClimateManager handleMessage(): do not handle: " + message);
			return;
		}
	}


	/**
	 * @param message
	 */
	private void handleGetTimeInfo(MaxTimeInformationMessage message) {
		if (!message.isRequest() || AmbientControlMW.getRoom().getMaxComponents().get(message.getFromAdress()) == null)
			return;
		System.out.println("ClimateManager - getTimeInfo: sending time to device: " + message.getFromAdress());
		MaxTimeInformationMessage time = getTimeInfoForDevice(new Date(), message.getFromAdress());
		queueManager.putOutMessage(time);

	}


	/**
	 * @param message
	 * @throws IOException
	 */
	private void handleSetTemperature(MaxSetTemperatureMessage message) {
		Thermostat thermostat = (Thermostat) AmbientControlMW.getRoom().getMaxComponents().get(message.getFromAdress());
		if (thermostat == null) {
			System.out.println("ClimateManager handleSetTemperature(): got request from unknown device: adress="
					+ message.getFromAdress());
			return;
		}

		RoomConfigurationFactory.beginTransaction();

		config.mode = message.getMode();
		config.setTemp = message.getTemp();
		config.temporaryUntilDate = message.getTemporaryUntil();

		RoomConfigurationFactory.commitTransaction();
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
	}


	/**
	 * @param message
	 * @throws IOException
	 */
	private void handlePairPing(MaxPairPingMessage message) {

		// known device wants to re-pair with some other device
		if (message.getToAdress().equals(config.vCubeAdress) == false && message.isReconnecting()) {
			System.out.println("ClimateManager handleMessage(): Device wants to refresh pairing with some other device: "
					+ message);
		}
		// device wants to repair with us
		else if (message.isReconnecting() && message.getToAdress().equals(config.vCubeAdress) == true) {
			// device is known and will be refreshed
			if (AmbientControlMW.getRoom().getMaxComponents().get(message.getFromAdress()) != null) {
				System.out.println("ClimateManager handleMessage(): Device wants to refresh pairing with us: " + message);
				sendPong(message, false);
			}
		}
		// device wants to pair and we are in learnmode. we create it
		// and set it up
		else if (learnMode && message.isReconnecting() == false) {
			sendPong(message, true);
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.messages.MessageListener#handleResponseMessages(org.
	 * ambientlight.messages.QeueManager.State,
	 * org.ambientlight.messages.Message, org.ambientlight.messages.Message)
	 */
	@Override
	public void onAckResponseMessage(State state, Message response, Message request) {
		if (response != null) {
			System.out.println("ClimateManager - handleResponseMessage: called with response: " + response);
		}
		MaxComponent device = AmbientControlMW.getRoom().getMaxComponents().get(((MaxMessage) request).getToAdress());
		if (device == null) {
			System.out.println("ClimateManager - handleResonseMessages: Device is unknown.");
			return;
		}

		RoomConfigurationFactory.beginTransaction();

		if (state == State.TIMED_OUT) {

			device.config.timedOut = true;
			System.out.println("Climate Manager - handleResponseMessage(): Error! Timeout for Device: " + device.config.label);

		} else if (state == State.RETRIEVED_ANSWER && response instanceof MaxAckMessage
				&& ((MaxAckMessage) response).getAckType() == MaxAckType.ACK_INVALID_MESSAGE) {

			device.config.invalidArgument = true;
			System.out.println("Climate Manager - handleResponseMessage(): Device: Error! " + device.config.label
					+ " reported invalid Arguments!");
		}

		RoomConfigurationFactory.commitTransaction();
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
	}


	/**
	 * @param message
	 * @throws IOException
	 */
	private void handleShutterState(MaxShutterContactStateMessage message) {
		ShutterContact shutterContact = (ShutterContact) AmbientControlMW.getRoom().getMaxComponents()
				.get(message.getFromAdress());
		if (shutterContact == null || message.getToAdress() != this.config.vCubeAdress) {
			System.out.println("ClimateManager handleShutterState(): got request from unknown device: adress="
					+ message.getFromAdress());
			return;
		}

		// MaxAckMessage ack = new MaxAckMessage();
		// ack.setFromAdress(this.config.vCubeAdress);
		// ack.setToAdress(message.getFromAdress());
		// ack.setSequenceNumber(getNewSequnceNumber());
		// ack.setAckType(MaxAckType.ACK_SIMPLE);
		// this.queueManager.putOutMessage(ack);

		RoomConfigurationFactory.beginTransaction();

		ShutterContact shutter = (ShutterContact) AmbientControlMW.getRoom().getMaxComponents().get(message.getFromAdress());
		shutter.config.batteryLow = message.isBatteryLow();
		shutter.isOpen = message.isOpen();
		shutter.config.rfError = message.hadRfError();
		shutter.config.lastUpdate = new Date(System.currentTimeMillis());

		RoomConfigurationFactory.commitTransaction();
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
	}


	/**
	 * @param message
	 * @throws IOException
	 */
	private void handleThermostatState(MaxThermostatStateMessage message) {

		Thermostat thermostat = (Thermostat) AmbientControlMW.getRoom().getMaxComponents().get(message.getFromAdress());
		if (thermostat == null) {
			System.out.println("ClimateManager handleThermostatState(): got request from unknown device: adress="
					+ message.getFromAdress());
			return;
		}

		RoomConfigurationFactory.beginTransaction();

		thermostat.config.batteryLow = message.isBatteryLow();
		thermostat.isLocked = message.isLocked();
		thermostat.config.lastUpdate = new Date(System.currentTimeMillis());

		thermostat.temperatur = message.getActualTemp();

		config.mode = message.getMode();
		config.temporaryUntilDate = message.getTemporaryUntil();
		config.setTemp = message.getSetTemp();

		RoomConfigurationFactory.commitTransaction();
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
	}


	public void setFactoryResetDevice(int adress) throws IOException {

		MaxComponent device = AmbientControlMW.getRoom().getMaxComponents().get(adress);
		if (device == null) {
			System.out.println("ClimateManager setFactoryResetDevice(): got request for unknown device: adress=" + adress);
			return;
		}

		RoomConfigurationFactory.beginTransaction();

		// Wait until shutterContact comes alive
		WaitForShutterContactCondition condition = null;
		if (device instanceof ShutterContact) {
			condition = new WaitForShutterContactCondition(device.config.adress, this.config.vCubeAdress);
			// MaxWakeUpMessage wakeUp = new MaxWakeUpMessage();
			// wakeUp.setFromAdress(this.config.vCubeAdress);
			// wakeUp.setToAdress(adress);
			// wakeUp.setSequenceNumber(getNewSequnceNumber());
			// AmbientControlMW.getRoom().qeueManager.putOutMessage(wakeUp,
			// condition);
		}

		// remove correlator
		UnRegisterCorrelatorMessage unRegisterCorelator = new MaxUnregisterCorrelationMessage(DispatcherType.MAX, adress,
				this.config.vCubeAdress);
		AmbientControlMW.getRoom().qeueManager.putOutMessage(unRegisterCorelator);

		// send remove
		MaxFactoryResetMessage resetDevice = getFactoryResetMessageForDevice(device.config.adress);
		AmbientControlMW.getRoom().qeueManager.putOutMessage(resetDevice, condition);

		// unregister link from other devices
		for (MaxComponentConfiguration currentConfig : config.devices.values()) {

			// we skip the device because it will be deleted anyway
			if (currentConfig.adress == adress) {
				continue;
			}

			// Wait until shutterContact comes alive if it is one
			WaitForShutterContactCondition conditionForCurrent = null;
			if (currentConfig instanceof ShutterContactConfiguration) {
				conditionForCurrent = new WaitForShutterContactCondition(currentConfig.adress, adress);
			}

			MaxRemoveLinkPartnerMessage unlink = getUnlinkMessageForDevice(currentConfig.adress, device.config.adress,
					device.config.getDeviceType());
			AmbientControlMW.getRoom().qeueManager.putOutMessage(unlink, conditionForCurrent);
		}

		// Remove from modell
		AmbientControlMW.getRoom().getMaxComponents().remove(adress);
		this.config.devices.remove(adress);

		RoomConfigurationFactory.commitTransaction();
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
	}


	/**
	 * @param adress
	 * @return
	 */
	private MaxFactoryResetMessage getFactoryResetMessageForDevice(int adress) {
		MaxFactoryResetMessage resetDevice = new MaxFactoryResetMessage();
		resetDevice.setFromAdress(config.vCubeAdress);
		resetDevice.setToAdress(adress);
		resetDevice.setSequenceNumber(getNewSequnceNumber());
		return resetDevice;
	}


	/**
	 * @param device
	 * @param currentConfig
	 * @return
	 */
	private MaxRemoveLinkPartnerMessage getUnlinkMessageForDevice(int adress, int linkPartnerAdress,
			DeviceType linkPartnerDeviceType) {
		MaxRemoveLinkPartnerMessage unlink = new MaxRemoveLinkPartnerMessage();
		unlink.setFromAdress(config.vCubeAdress);
		unlink.setToAdress(adress);
		unlink.setSequenceNumber(getNewSequnceNumber());
		unlink.setLinkPartnerAdress(linkPartnerAdress);
		unlink.setLinkPartnerDeviceType(linkPartnerDeviceType);
		return unlink;
	}


	private void sendTimeInfoToComponents() {
		Date now = new Date();
		List<Message> messages = new ArrayList<Message>();

		for (MaxComponent current : AmbientControlMW.getRoom().getMaxComponents().values()) {
			if (current instanceof Thermostat) {
				MaxTimeInformationMessage message = getTimeInfoForDevice(now, current.config.adress);

				messages.add(message);
			}
		}
		AmbientControlMW.getRoom().qeueManager.putOutMessages(messages);
	}


	/**
	 * @param now
	 * @param current
	 * @return
	 */
	private MaxTimeInformationMessage getTimeInfoForDevice(Date now, int adress) {
		MaxTimeInformationMessage message = new MaxTimeInformationMessage();
		message.setSequenceNumber(getNewSequnceNumber());
		message.setFromAdress(config.vCubeAdress);
		message.setToAdress(adress);
		message.setTime(now);
		return message;
	}


	/**
	 * @param pairMessage
	 * @param newDevice
	 */
	private void sendPong(MaxPairPingMessage pairMessage, boolean newDevice) {

		// check for known deviceTypes
		if (pairMessage.getDeviceType() == null && pairMessage.getDeviceType() != DeviceType.HEATING_THERMOSTAT
				&& pairMessage.getDeviceType() != DeviceType.HEATING_THERMOSTAT_PLUS
				&& pairMessage.getDeviceType() != DeviceType.SHUTTER_CONTACT) {
			System.out.println("ClimateManager - sendPong(): We do not support this device");
			return;
		}

		// Send pair pong
		MaxPairPongMessage pairPong = new MaxPairPongMessage();
		pairPong.setFromAdress(config.vCubeAdress);
		pairPong.setToAdress(pairMessage.getFromAdress());

		pairPong.setSequenceNumber(pairMessage.getSequenceNumber());
		AmbientControlMW.getRoom().qeueManager.putOutMessage(pairPong);

		if (newDevice) {
			// keep device waiting for messages
			MaxWakeUpMessage wakeUp = new MaxWakeUpMessage();
			wakeUp.setFromAdress(this.config.vCubeAdress);
			wakeUp.setSequenceNumber(getNewSequnceNumber());
			wakeUp.setToAdress(pairMessage.getFromAdress());
			AmbientControlMW.getRoom().qeueManager.putOutMessage(wakeUp);

			// add and setup new device
			RoomConfigurationFactory.beginTransaction();
			List<ConditionalMessage> outMessages = new ArrayList<ConditionalMessage>();

			// register the device at the rfmbridge - it will ack some requests
			// directly because the way over network is to long. eg.
			// shuttercontact messages.
			outMessages.add(new ConditionalMessage(null, new MaxRegisterCorrelationMessage(DispatcherType.MAX, pairMessage
					.getFromAdress(), this.config.vCubeAdress)));

			// these values will be filled
			MaxComponentConfiguration config = null;
			MaxComponent device = null;

			// setup and create device and config specific for each devicetype
			if (pairMessage.getDeviceType() == DeviceType.HEATING_THERMOSTAT
					|| pairMessage.getDeviceType() == DeviceType.HEATING_THERMOSTAT_PLUS) {
				device = new Thermostat();
				config = new ThermostatConfiguration();
				device.config = config;

				// it is the sensor value we can read. but we need something to
				// start withString.valueOf(
				((Thermostat) device).temperatur = this.config.setTemp;

				((ThermostatConfiguration) config).offset = this.config.DEFAULT_OFFSET;
				config.label = "Thermostat";

				// Setup Time;
				outMessages.add(new ConditionalMessage(null, getTimeInfoForDevice(new Date(), pairMessage.getFromAdress())));

				// Setup valve
				outMessages.add(new ConditionalMessage(null, getConfigValveForDevice(pairMessage.getFromAdress())));

				// Set Temperatures
				outMessages.add(new ConditionalMessage(null, getConfigureTemperatures(pairMessage.getFromAdress())));

				// Setup temperatur
				outMessages.add(new ConditionalMessage(null, getSetTempForDevice(pairMessage.getFromAdress())));

				// Set group
				outMessages.add(new ConditionalMessage(null, getSetGroupIdForDevice(pairMessage.getFromAdress())));

				// Setup weekly Profile
				List<Message> weekProfile = getWeekProfileForDevice(pairMessage.getFromAdress(), this.config.currentWeekProfile);
				for (Message dayProfile : weekProfile) {
					outMessages.add(new ConditionalMessage(null, dayProfile));
				}

			} else if (pairMessage.getDeviceType() == DeviceType.SHUTTER_CONTACT) {
				device = new ShutterContact();
				config = new ShutterContactConfiguration();
				device.config = config;
				((ShutterContact) device).isOpen = false;
				config.label = "Fensterkontakt";
			}

			// set common values to the configuration
			config.adress = pairMessage.getFromAdress();
			config.batteryLow = false;
			config.firmware = pairMessage.getFirmware();
			config.invalidArgument = false;
			config.lastUpdate = new Date();
			config.rfError = false;
			config.serial = pairMessage.getSerial();
			config.timedOut = false;

			// add device to ambientcontrol
			device.config = config;
			AmbientControlMW.getRoom().getMaxComponents().put(config.adress, device);
			AmbientControlMW.getRoom().config.climate.devices.put(config.adress, config);

			// link devices
			for (MaxComponentConfiguration currentConfig : this.config.devices.values()) {

				// do not link with ourself
				if (currentConfig.adress == pairMessage.getFromAdress()) {
					continue;
				}

				// never link shutter contacts with each other
				if (currentConfig instanceof ShutterContactConfiguration
						&& pairMessage.getDeviceType() == DeviceType.SHUTTER_CONTACT) {
					continue;
				}

				// if current device is a shuttercontact - wait until its online
				// again
				WaitForShutterContactCondition conditionForCurrent = null;
				if (currentConfig instanceof ShutterContactConfiguration) {
					conditionForCurrent = new WaitForShutterContactCondition(currentConfig.adress, this.config.vCubeAdress);
				}

				// link new device to current
				MaxAddLinkPartnerMessage linkCurrentToNew = getLinkMessage(currentConfig.adress, pairMessage.getFromAdress(),
						pairMessage.getDeviceType());
				outMessages.add(new ConditionalMessage(conditionForCurrent, linkCurrentToNew));

				// link the current to the new device - the device was woken up
				// no condition is needed
				MaxAddLinkPartnerMessage linkNewToCurrent = getLinkMessage(pairMessage.getFromAdress(), currentConfig.adress,
						currentConfig.getDeviceType());
				outMessages.add(new ConditionalMessage(null, linkNewToCurrent));
			}

			queueManager.putOutMessagesWithCondition(outMessages);
			RoomConfigurationFactory.commitTransaction();
			AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
		}
	}


	/**
	 * @param pairMessage
	 * @param config
	 * @param currentConfig
	 * @return
	 */
	private MaxAddLinkPartnerMessage getLinkMessage(int adress, int linkPartnerAdress, DeviceType linkPartnerTDeviceType) {
		MaxAddLinkPartnerMessage linkCurrentToNew = new MaxAddLinkPartnerMessage();
		linkCurrentToNew.setSequenceNumber(getNewSequnceNumber());
		linkCurrentToNew.setFromAdress(this.config.vCubeAdress);
		linkCurrentToNew.setToAdress(adress);
		linkCurrentToNew.setLinkPartnerAdress(linkPartnerAdress);
		linkCurrentToNew.setLinkPartnerDeviceType(linkPartnerTDeviceType);
		return linkCurrentToNew;
	}


	/**
	 * @param pairMessage
	 * @return
	 */
	private MaxSetGroupIdMessage getSetGroupIdForDevice(int adress) {
		MaxSetGroupIdMessage group = new MaxSetGroupIdMessage();
		group.setSequenceNumber(getNewSequnceNumber());
		group.setFromAdress(this.config.vCubeAdress);
		group.setToAdress(adress);
		group.setGroupId(this.config.groupId);
		return group;
	}


	/**
	 * @param pairMessage
	 * @return
	 */
	private MaxConfigureTemperaturesMessage getConfigureTemperatures(int adress) {
		MaxConfigureTemperaturesMessage temps = new MaxConfigureTemperaturesMessage();
		temps.setSequenceNumber(getNewSequnceNumber());
		temps.setFromAdress(this.config.vCubeAdress);
		temps.setToAdress(adress);
		temps.setComfortTemp(this.config.comfortTemperatur);
		temps.setEcoTemp(this.config.ecoTemperatur);
		temps.setMaxTemp(this.config.maxTemp);
		temps.setMinTemp(this.config.minTemp);
		temps.setOffsetTemp(this.config.DEFAULT_OFFSET);
		temps.setWindowOpenTemp(this.config.windowOpenTemperatur);
		temps.setWindowOpenTime(this.config.windowOpenTimeMins);
		return temps;
	}


	/**
	 * @param pairMessage
	 * @return
	 */
	private MaxConfigValveMessage getConfigValveForDevice(int adress) {
		MaxConfigValveMessage valve = new MaxConfigValveMessage();
		valve.setSequenceNumber(getNewSequnceNumber());
		valve.setFromAdress(this.config.vCubeAdress);
		valve.setToAdress(adress);
		valve.setBoostDuration(this.config.boostDurationMins);
		valve.setBoostValvePosition(this.config.boostValvePositionPercent);
		DecalcEntry decalc = valve.new DecalcEntry();
		decalc.day = this.config.decalcDay;
		decalc.hour = this.config.decalcHour;
		valve.setDecalc(decalc);
		valve.setMaxValvePosition(this.config.DEFAULT_MAX_VALVE_POSITION);
		valve.setValveOffset(this.config.DEFAULT_VALVE_OFFSET_PERCENT);
		return valve;
	}


	public void setCurrentProfile(String profile) {
		RoomConfigurationFactory.beginTransaction();

		if (config.weekProfiles.containsKey(profile) == false || config.weekProfiles.get(profile).isEmpty())
			throw new IllegalArgumentException("the selected weekProfile does not exist or is empty and unusable!");

		config.currentWeekProfile = profile;

		List<Message> messages = new ArrayList<Message>();

		for (MaxComponent current : AmbientControlMW.getRoom().getMaxComponents().values()) {
			if (current instanceof Thermostat) {
				messages.addAll(getWeekProfileForDevice(current.config.adress, profile));
			}
		}

		queueManager.putOutMessages(messages);

		RoomConfigurationFactory.commitTransaction();
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();

	}


	public void setMode(float temp, MaxThermostateMode mode, Date until) {
		RoomConfigurationFactory.beginTransaction();

		if (until != null && config.mode != MaxThermostateMode.TEMPORARY)
			throw new IllegalArgumentException("An until date may only be set in temporary mode.");

		config.mode = mode;
		config.setTemp = temp;
		config.temporaryUntilDate = until;

		List<Message> messages = new ArrayList<Message>();

		for (MaxComponent current : AmbientControlMW.getRoom().getMaxComponents().values()) {
			if (current instanceof Thermostat) {
				MaxSetTemperatureMessage outMessage = getSetTempForDevice(current.config.adress);
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
			config.setTemp = nextDayEntry.getTemp();
		}

		RoomConfigurationFactory.commitTransaction();
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
	}


	/**
	 * @param current
	 * @return
	 */
	private MaxSetTemperatureMessage getSetTempForDevice(int adress) {
		MaxSetTemperatureMessage outMessage = new MaxSetTemperatureMessage();
		outMessage.setFromAdress(config.vCubeAdress);
		outMessage.setTemp(config.setTemp);
		outMessage.setTemporaryUntil(config.temporaryUntilDate);
		outMessage.setMode(config.mode);
		outMessage.setSequenceNumber(getNewSequnceNumber());
		outMessage.setToAdress(adress);
		return outMessage;
	}


	/**
	 * @param fromAdress
	 * @param weekProfile
	 */
	private List<Message> getWeekProfileForDevice(Integer deviceAdress, String weekProfile) {
		List<Message> messages = new ArrayList<Message>();
		HashMap<MaxDayInWeek, List<DayEntry>> profiles = config.weekProfiles.get(weekProfile);
		for (Entry<MaxDayInWeek, List<DayEntry>> currentDayProfile : profiles.entrySet()) {
			int entryCountPartOne = currentDayProfile.getValue().size();
			boolean twoParts = false;

			if (entryCountPartOne > 7) {
				twoParts = true;
				entryCountPartOne = 7;
			}

			MaxConfigureWeekProgrammMessage week = new MaxConfigureWeekProgrammMessage();
			for (int i = 0; i < entryCountPartOne; i++) {
				week.addEntry(currentDayProfile.getValue().get(i));
			}
			week.setDay(currentDayProfile.getKey());
			week.setFromAdress(config.vCubeAdress);
			week.setSecondPart(false);
			week.setSequenceNumber(getNewSequnceNumber());
			week.setToAdress(deviceAdress);
			messages.add(week);

			if (twoParts) {
				MaxConfigureWeekProgrammMessage week2 = new MaxConfigureWeekProgrammMessage();
				for (int i = 7; i < currentDayProfile.getValue().size(); i++) {
					week2.addEntry(currentDayProfile.getValue().get(i));
				}
				week2.setDay(currentDayProfile.getKey());
				week2.setFromAdress(config.vCubeAdress);
				week2.setSecondPart(true);
				week2.setSecondPart(true);
				week2.setSequenceNumber(getNewSequnceNumber());
				week2.setToAdress(deviceAdress);
				messages.add(week2);
			}
		}
		return messages;
	}


	public void startPairingMode() {
		this.learnMode = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					System.out.println("ClimateManager - startPairing(): Waiting for new Devices");
					Thread.sleep(WAIT_FOR_NEW_DEVICES * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					learnMode = false;
					System.out.println("ClimateManager - startPairing(): Stopped waiting for new Devices");
				}
			}
		}).start();
	}


	private int getNewSequnceNumber() {
		outSequenceNumber++;
		if (outSequenceNumber > 255) {
			outSequenceNumber = 0;
		}

		return outSequenceNumber;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.messages.MessageListener#onConnectionLost(org.ambientlight
	 * .messages.DispatcherType)
	 */
	@Override
	public void onDisconnectDispatcher(DispatcherType dispatcher) {

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.MessageListener#onConnectionRecovered(org.
	 * ambientlight.messages.DispatcherType)
	 */
	@Override
	public void onConnectDispatcher(DispatcherType dispatcher) {
		System.out.println("ClimateManager - onConnectDispatcher(): got connection. Syncing MAX devices.");
		if (dispatcher == DispatcherType.MAX) {
			this.sendRegisterCorrelators();

			this.sendTimeInfoToComponents();

			this.setMode(config.setTemp, config.mode, config.temporaryUntilDate);
		}
	}
}
