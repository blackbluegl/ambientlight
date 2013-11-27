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
import org.ambientlight.messages.DispatcherType;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.MessageListener;
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
import org.ambientlight.messages.max.MaxRemoveLinkPartnerMessage;
import org.ambientlight.messages.max.MaxSetGroupIdMessage;
import org.ambientlight.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.messages.max.MaxShutterContactStateMessage;
import org.ambientlight.messages.max.MaxThermostatStateMessage;
import org.ambientlight.messages.max.MaxThermostateMode;
import org.ambientlight.messages.max.MaxTimeInformationMessage;
import org.ambientlight.messages.max.MaxWakeUpMessage;
import org.ambientlight.messages.max.WaitForShutterContactCondition;
import org.ambientlight.messages.rfm22bridge.RegisterCorrelatorMessage;
import org.ambientlight.messages.rfm22bridge.UnRegisterCorrelatorMessage;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.entities.MaxComponent;
import org.ambientlight.room.entities.ShutterContact;
import org.ambientlight.room.entities.Thermostat;


/**
 * @author Florian Bornkessel
 * 
 */
public class ClimateManager implements MessageListener {

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


	public ClimateManager() {

		Timer timer = new Timer();
		Calendar threePm = GregorianCalendar.getInstance();
		threePm.set(Calendar.HOUR_OF_DAY, 3);
		threePm.set(Calendar.MINUTE, 5);
		timer.scheduleAtFixedRate(syncTimeTask, threePm.getTime(), 24 * 60 * 60 * 1000);

		List<Message> correlators = new ArrayList<Message>();
		for (MaxComponentConfiguration currentDeviceConfig : config.devices.values()) {
			correlators.add(new RegisterCorrelatorMessage(DispatcherType.MAX, String.valueOf(currentDeviceConfig.adress)));
		}
		AmbientControlMW.getRoom().qeueManager.putOutMessages(correlators);

		sendTimeInfoToComponents();

		setMode(config.setTemp, config.mode, config.temporaryUntilDate);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.messages.MessageListener#handleMessage(org.ambientlight
	 * .messages.Message)
	 */
	@Override
	public void handleMessage(Message message) {
		if (message instanceof MaxThermostatStateMessage) {
			handleThermostatState((MaxThermostatStateMessage) message);
		} else if (message instanceof MaxSetTemperatureMessage) {
			handleSetTemperature((MaxSetTemperatureMessage) message);
		} else if (message instanceof MaxShutterContactStateMessage) {
			handleShutterState((MaxShutterContactStateMessage) message);
		} else if (message instanceof MaxPairPingMessage) {
			handlePairPing((MaxPairPingMessage) message);
		} else if (message instanceof MaxMessage == false) {
			System.out.println("ClimateManager handleMessage(): do not handle: " + message);
			return;
		}
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
		if (message.getToAdress().equals(config.vCubeAdress) == false) {
			System.out.println("ClimateManager handleMessage(): Device wants to refresh pairing with some other device: "
					+ message);
		}
		// device wants to repair with us
		else if (learnMode == false && message.isReconnecting() && message.getToAdress().equals(config.vCubeAdress) == true) {
			// device is known and will be refreshed
			if (AmbientControlMW.getRoom().getMaxComponents().get(message.getFromAdress()) != null) {
				System.out.println("ClimateManager handleMessage(): Device wants to refresh pairing with us: " + message);
				sendPong(message, false);
				// device is unknown. we recreate it and set it up
			} else {
				System.out.println("ClimateManager handleMessage(): Unknown Device refreshes pairing with us. Recreate it now: "
						+ message);
				sendPong(message, true);
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
	public void handleResponseMessages(State state, Message response, Message request) {

		RoomConfigurationFactory.beginTransaction();

		MaxComponent device = AmbientControlMW.getRoom().getMaxComponents().get(((MaxMessage) request).getToAdress());

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
		RoomConfigurationFactory.beginTransaction();

		ShutterContact shutter = (ShutterContact) AmbientControlMW.getRoom().getMaxComponents().get(message.getFromAdress());
		shutter.config.batteryLow = message.isBatteryLow();
		shutter.isOpen = message.isOpen();
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

		// send remove
		MaxFactoryResetMessage resetDevice = new MaxFactoryResetMessage();
		resetDevice.setFromAdress(config.vCubeAdress);
		resetDevice.setSequenceNumber(getNewSequnceNumber());
		resetDevice.setToAdress(adress);

		// Wait until shutterContact comes alive
		WaitForShutterContactCondition condition = null;
		if (device.config instanceof ShutterContactConfiguration) {
			condition = new WaitForShutterContactCondition(device.config.adress);
		}

		AmbientControlMW.getRoom().qeueManager.putOutMessage(resetDevice, condition);

		// unregister link from other devices
		for (MaxComponentConfiguration currentConfig : config.devices.values()) {

			MaxRemoveLinkPartnerMessage unlink = new MaxRemoveLinkPartnerMessage();
			unlink.setLinkPartnerAdress(currentConfig.adress);
			unlink.setLinkPartnerDeviceType(device.config.getDeviceType());
			unlink.setSequenceNumber(getNewSequnceNumber());
			unlink.setFromAdress(config.vCubeAdress);

			// Wait until shutterContact comes alive
			WaitForShutterContactCondition conditionForCurrent = null;
			if (currentConfig instanceof ShutterContactConfiguration) {
				conditionForCurrent = new WaitForShutterContactCondition(currentConfig.adress);
			}

			AmbientControlMW.getRoom().qeueManager.putOutMessage(unlink, conditionForCurrent);
		}

		// remove correlator
		UnRegisterCorrelatorMessage corelator = new UnRegisterCorrelatorMessage();
		corelator.setDispatcherType(DispatcherType.MAX);
		corelator.setCorrelator(String.valueOf(adress));

		// Remove from modell
		AmbientControlMW.getRoom().getMaxComponents().remove(adress);
		config.devices.remove(adress);

		RoomConfigurationFactory.commitTransaction();
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
	}


	private void sendTimeInfoToComponents() {
		Date now = new Date();
		List<Message> messages = new ArrayList<Message>();

		for (MaxComponent current : AmbientControlMW.getRoom().getMaxComponents().values()) {
			if (current instanceof Thermostat) {
				MaxTimeInformationMessage message = new MaxTimeInformationMessage();
				message.setSequenceNumber(getNewSequnceNumber());
				message.setFromAdress(config.vCubeAdress);
				message.setTime(now);
				message.setToAdress(((Thermostat) current).config.adress);

				messages.add(message);
			}
		}
		AmbientControlMW.getRoom().qeueManager.putOutMessages(messages);
	}


	/**
	 * @param pairMessage
	 * @param newDevice
	 */
	private void sendPong(MaxPairPingMessage pairMessage, boolean newDevice) {
		RoomConfigurationFactory.beginTransaction();

		// check for known deviceType
		if (pairMessage.getDeviceType() == null) {
			System.out.println("ClimateManager - sendPong(): We do not support this device");
			return;
		}

		List<Message> outMessages = new ArrayList<Message>();

		// Send pair Pong
		MaxPairPongMessage pairPong = new MaxPairPongMessage();
		pairPong.setFromAdress(config.vCubeAdress);
		pairPong.setToAdress(pairMessage.getFromAdress());
		pairPong.setSequenceNumber(pairMessage.getSequenceNumber());
		outMessages.add(pairPong);

		if (newDevice) {
			MaxComponentConfiguration config = null;
			MaxComponent device = null;

			if (pairMessage.getDeviceType() == DeviceType.HEATING_THERMOSTAT
					|| pairMessage.getDeviceType() == DeviceType.HEATING_THERMOSTAT_PLUS) {
				device = new Thermostat();
				config = new ThermostatConfiguration();
				device.config = config;

				((Thermostat) device).temperatur = this.config.setTemp;

				((ThermostatConfiguration) config).offset = 0;
				config.label = "Thermostat";

				// Wake Device up to save timeslots
				MaxWakeUpMessage wakeUp = new MaxWakeUpMessage();
				wakeUp.setFromAdress(this.config.vCubeAdress);
				wakeUp.setSequenceNumber(getNewSequnceNumber());
				wakeUp.setToAdress(pairMessage.getFromAdress());
				outMessages.add(wakeUp);

				// Setup Time;
				MaxTimeInformationMessage timeMessage = new MaxTimeInformationMessage();
				timeMessage.setFromAdress(this.config.vCubeAdress);
				timeMessage.setSequenceNumber(getNewSequnceNumber());
				timeMessage.setToAdress(pairMessage.getFromAdress());
				timeMessage.setTime(new Date());
				outMessages.add(timeMessage);

				// Setup valve
				MaxConfigValveMessage valve = new MaxConfigValveMessage();
				valve.setBoostDuration(this.config.boostDurationMins);
				valve.setBoostValvePosition(this.config.boostValvePositionPercent);
				DecalcEntry decalc = valve.new DecalcEntry();
				decalc.day = this.config.decalcDay;
				decalc.hour = this.config.decalcHour;
				valve.setDecalc(decalc);
				valve.setFromAdress(this.config.vCubeAdress);
				valve.setMaxValvePosition(this.config.maxValvePosition);
				valve.setSequenceNumber(getNewSequnceNumber());
				valve.setToAdress(pairMessage.getFromAdress());
				valve.setValveOffset(this.config.valveOffsetPercent);
				outMessages.add(valve);

				// Set Temperatures
				MaxConfigureTemperaturesMessage temps = new MaxConfigureTemperaturesMessage();
				temps.setComfortTemp(this.config.comfortTemperatur);
				temps.setEcoTemp(this.config.ecoTemperatur);
				temps.setFromAdress(this.config.vCubeAdress);
				temps.setMaxTemp(this.config.maxTemp);
				temps.setMinTemp(this.config.minTemp);
				temps.setOffsetTemp(this.config.defaultOffset);
				temps.setSequenceNumber(getNewSequnceNumber());
				temps.setToAdress(pairMessage.getFromAdress());
				temps.setWindowOpenTemp(this.config.windowOpenTemperatur);
				temps.setWindowOpenTime(this.config.windowOpenTimeMins);
				outMessages.add(temps);

				// Setup temperatur
				outMessages.add(getSetTempForDevice(pairMessage.getFromAdress()));

				// Setup weekly Profile
				outMessages.addAll(getWeekProfileForDevice(pairMessage.getFromAdress(), this.config.currentWeekProfile));

			} else if (pairMessage.getDeviceType() == DeviceType.SHUTTER_CONTACT) {
				device = new ShutterContact();
				config = new ShutterContactConfiguration();
				device.config = config;

				((ShutterContact) device).isOpen = false;

				config.label = "Fensterkontakt";
			}

			// set common values to the configuration
			if (device != null) {
				config.adress = pairMessage.getFromAdress();
				config.batteryLow = false;
				config.firmware = pairMessage.getFirmware();
				config.invalidArgument = false;
				config.lastUpdate = new Date();
				config.rfError = false;
				config.serial = pairMessage.getSerial();
				config.timedOut = false;

				AmbientControlMW.getRoom().getMaxComponents().put(config.adress, device);
				AmbientControlMW.getRoom().config.climate.devices.put(config.adress, config);

				// Set group
				MaxSetGroupIdMessage group = new MaxSetGroupIdMessage();
				group.setFromAdress(this.config.vCubeAdress);
				group.setGroupId(this.config.groupId);
				group.setSequenceNumber(getNewSequnceNumber());
				group.setToAdress(pairMessage.getFromAdress());
				outMessages.add(group);

				// link devices
				for (MaxComponentConfiguration currentConfig : this.config.devices.values()) {

					// only pair with shutterContacts or Thermostates
					if (currentConfig instanceof ShutterContactConfiguration == false
							&& currentConfig instanceof ThermostatConfiguration == false) {
						continue;
					}

					// do not pair with other window contacts
					if (pairMessage.getDeviceType() == DeviceType.SHUTTER_CONTACT
							&& currentConfig instanceof ShutterContactConfiguration) {
						continue;
					}

					// do not pair with ourself
					if (currentConfig.adress == pairMessage.getFromAdress()) {
						continue;
					}

					MaxAddLinkPartnerMessage link = new MaxAddLinkPartnerMessage();
					link.setLinkPartnerAdress(currentConfig.adress);
					link.setLinkPartnerDeviceType(pairMessage.getDeviceType());
					AmbientControlMW.getRoom().qeueManager.putOutMessage(link);
				}
			}
		}

		AmbientControlMW.getRoom().qeueManager.putOutMessages(outMessages);

		RoomConfigurationFactory.commitTransaction();
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
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

		AmbientControlMW.getRoom().qeueManager.putOutMessages(messages);

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

		AmbientControlMW.getRoom().qeueManager.putOutMessages(messages);

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
					Thread.sleep(WAIT_FOR_NEW_DEVICES * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					learnMode = false;
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
}
