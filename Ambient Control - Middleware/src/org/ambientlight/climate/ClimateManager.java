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
import org.ambientlight.config.process.events.AlarmEvent;
import org.ambientlight.config.process.events.Event;
import org.ambientlight.config.room.ClimateConfiguration;
import org.ambientlight.config.room.actors.MaxComponentConfiguration;
import org.ambientlight.config.room.actors.ShutterContactConfiguration;
import org.ambientlight.config.room.actors.ThermostatConfiguration;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.MessageListener;
import org.ambientlight.messages.QeueManager.State;
import org.ambientlight.messages.max.DeviceType;
import org.ambientlight.messages.max.MaxAckMessage;
import org.ambientlight.messages.max.MaxAckType;
import org.ambientlight.messages.max.MaxConfigValveMessage;
import org.ambientlight.messages.max.MaxConfigValveMessage.DecalcEntry;
import org.ambientlight.messages.max.MaxConfigureTemperaturesMessage;
import org.ambientlight.messages.max.MaxConfigureWeekProgrammMessage;
import org.ambientlight.messages.max.MaxConfigureWeekProgrammMessage.DayEntry;
import org.ambientlight.messages.max.MaxDayInWeek;
import org.ambientlight.messages.max.MaxMessage;
import org.ambientlight.messages.max.MaxPairPingMessage;
import org.ambientlight.messages.max.MaxPairPongMessage;
import org.ambientlight.messages.max.MaxSetGroupIdMessage;
import org.ambientlight.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.messages.max.MaxShutterContactStateMessage;
import org.ambientlight.messages.max.MaxThermostatStateMessage;
import org.ambientlight.messages.max.MaxThermostateMode;
import org.ambientlight.messages.max.MaxTimeInformationMessage;
import org.ambientlight.messages.max.MaxWakeUpMessage;
import org.ambientlight.process.eventmanager.IEventListener;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.entities.MaxComponent;
import org.ambientlight.room.entities.ShutterContact;
import org.ambientlight.room.entities.Thermostat;


/**
 * @author Florian Bornkessel
 * 
 */
public class ClimateManager implements MessageListener, IEventListener {

	AlarmEvent alarmForTimeSetting = new AlarmEvent(3, 5, "climateManager.SetTimeInfo");

	public ClimateConfiguration config;

	private int outSequenceNumber = 0;

	boolean learnMode = false;
	public static int WAIT_FOR_NEW_DEVICES = 90;


	public ClimateManager() {
		setTimeInfoToComponents();
		registerAlarmForTimeSetting();
	}

	private ModeTracker modeTracker = new ModeTracker();

	class ModeTracker {

		public MaxThermostateMode oldMode;
		public Date oldUntil;
		public float oldTemp;
		public boolean run = true;
		public DayEntry nextDayEntry;

		TimerTask boosterEndTask = new TimerTask() {

			@Override
			public void run() {
				config.mode = oldMode;
				config.temporaryUntilDate = oldUntil;
				AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
			}
		};

		TimerTask temporaryTask = new TimerTask() {

			@Override
			public void run() {
				config.setTemp = oldTemp;
				config.mode = oldMode;
				config.temporaryUntilDate = null;
				AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
			}
		};

		TimerTask autoTask = new TimerTask() {

			@Override
			public void run() {
				config.setTemp = nextDayEntry.getTemp();
				config.mode = MaxThermostateMode.AUTO;
				config.temporaryUntilDate = null;
				AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
				// get shure that we will be called again for the next
				// temperature change
				handleMode(config.setTemp, MaxThermostateMode.AUTO, null);
			}
		};


		void before() {
			oldTemp = config.setTemp;
			oldMode = config.mode;
			oldUntil = config.temporaryUntilDate;
		}


		void handleMode(float temp, MaxThermostateMode mode, Date until) {
			boosterEndTask.cancel();
			temporaryTask.cancel();
			autoTask.cancel();

			// keep old values and reset after boost is over
			if (mode == MaxThermostateMode.BOOST) {
				Calendar cal = GregorianCalendar.getInstance();
				cal.add(Calendar.MINUTE, config.boostDurationMins);
				Timer timer = new Timer();
				timer.schedule(boosterEndTask, cal.getTime());
			}

			// in manual mode we do not have to do anything.
			if (mode == MaxThermostateMode.MANUAL) {
			}

			// restores last mode after temporary mode is over
			if (mode == MaxThermostateMode.TEMPORARY) {
				Timer timer = new Timer();
				timer.schedule(temporaryTask, until);
			}

			if (mode == MaxThermostateMode.AUTO) {
				Calendar cal = GregorianCalendar.getInstance();
				MaxDayInWeek day = MaxDayInWeek.forCalendarDayInWeek(cal.get(Calendar.DAY_OF_WEEK));
				List<DayEntry> entries = config.weekProfiles.get(config.currentWeekProfile).get(day);
				int minutesOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60;
				minutesOfDay += cal.get(Calendar.MINUTE);
				int latestPossibleTime = 24 * 60;

				if (minutesOfDay == latestPossibleTime) {
					minutesOfDay = 0;
				}

				DayEntry nextDayEntry = null;
				for (DayEntry current : entries) {
					int currentMinutes = current.getHour() * 60 + current.getMin();
					if (currentMinutes > minutesOfDay && currentMinutes <= latestPossibleTime) {
						latestPossibleTime = currentMinutes;
						nextDayEntry = current;
					}
				}

				this.nextDayEntry = nextDayEntry;
				cal.set(Calendar.HOUR_OF_DAY, nextDayEntry.getHour());
				cal.set(Calendar.MINUTE, nextDayEntry.getMin());
				Timer timer = new Timer();
				timer.schedule(autoTask, cal.getTime());
			}
		}
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
		try {

			if (message instanceof MaxMessage == false) {
				System.out.println("ClimateManager handleMessage(): Errror! Got unknown message: " + message);
				return;
			}

			if (message instanceof MaxThermostatStateMessage) {
				handleThermostatState((MaxThermostatStateMessage) message);
			}

			if (message instanceof MaxShutterContactStateMessage) {
				handleShutterState((MaxShutterContactStateMessage) message);
			}
			if (message instanceof MaxPairPingMessage) {
				handlePairPing(message);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * @param message
	 * @throws IOException
	 */
	private void handlePairPing(Message message) throws IOException {
		boolean informCallbackClients = false;
		MaxPairPingMessage pairMessage = (MaxPairPingMessage) message;
		RoomConfigurationFactory.beginTransaction();
		// known device wants to re-pair with some other device
		if (learnMode == false && pairMessage.isReconnecting() && pairMessage.getToAdress().equals(config.vCubeAdress) == false) {
			System.out.println("ClimateManager handleMessage(): Device wants to refresh pairing with some other device: "
					+ message);
		}
		// device wants to repair with us
		else if (learnMode == false && pairMessage.isReconnecting()
				&& pairMessage.getToAdress().equals(config.vCubeAdress) == true) {
			// device is known and will be refreshed
			if (AmbientControlMW.getRoom().getMaxComponents().get(pairMessage.getFromAdress()) != null) {
				System.out.println("ClimateManager handleMessage(): Device wants to refresh pairing with us: " + message);
				createPong(pairMessage, false);
				// device is unknown. we recreate it and set it up
			} else {
				System.out
				.println("ClimateManager handleMessage(): Device wants to refresh pairing with us. But we don't know it and recreate it now: "
						+ message);
				createPong(pairMessage, true);
				informCallbackClients = true;
			}
		}
		// device wants to pair and we are in learnmode. we create it
		// and set it up
		else if (learnMode && pairMessage.isReconnecting() == false) {
			createPong(pairMessage, true);
			informCallbackClients = true;
		}
		RoomConfigurationFactory.commitTransaction();
		if (informCallbackClients) {
			AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
		}
	}


	/**
	 * @param pairMessage
	 * @param b
	 */
	private void createPong(MaxPairPingMessage pairMessage, boolean newDevice) {

		// check for known deviceType
		if (pairMessage.getDeviceType() == null)
			return;

		List<Message> outMessages = new ArrayList<Message>();

		if (newDevice) {
			MaxComponentConfiguration config = null;
			MaxComponent device = null;

			if (pairMessage.getDeviceType() == DeviceType.HEATING_THERMOSTAT
					|| pairMessage.getDeviceType() == DeviceType.HEATING_THERMOSTAT) {
				device = new Thermostat();
				config = new ThermostatConfiguration();
				device.config = config;

				((Thermostat) device).temperatur = this.config.setTemp;

				((ThermostatConfiguration) config).offset = 0;
				config.label = "Thermostat";

			} else if (pairMessage.getDeviceType() == DeviceType.SHUTTER_CONTACT) {
				device = new ShutterContact();
				config = new ShutterContactConfiguration();
				device.config = config;

				((ShutterContact) device).isOpen = false;

				config.label = "Fensterkontakt";
			}
			// only set the config if a device can be handled
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
			}
		}

		MaxPairPongMessage response = new MaxPairPongMessage();
		response.setFromAdress(config.vCubeAdress);
		response.setToAdress(pairMessage.getFromAdress());
		response.setSequenceNumber(pairMessage.getSequenceNumber());
		outMessages.add(response);

		// Set up if device is a Thermostat
		if ((pairMessage.getDeviceType() == DeviceType.HEATING_THERMOSTAT || pairMessage.getDeviceType() == DeviceType.HEATING_THERMOSTAT_PLUS)) {
			// Wake Device up and keep it up for 30 seconds - this will reduce
			// the bridges timeslots that are needed;
			MaxWakeUpMessage wakeUp = new MaxWakeUpMessage();
			wakeUp.setFromAdress(config.vCubeAdress);
			wakeUp.setSequenceNumber(getNewSequnceNumber());
			wakeUp.setToAdress(pairMessage.getFromAdress());
			outMessages.add(wakeUp);

			// Set group
			MaxSetGroupIdMessage group = new MaxSetGroupIdMessage();
			group.setFromAdress(config.vCubeAdress);
			group.setGroupId(config.groupId);
			group.setSequenceNumber(getNewSequnceNumber());
			group.setToAdress(pairMessage.getFromAdress());
			outMessages.add(group);

			// Setup Time;
			MaxTimeInformationMessage timeMessage = new MaxTimeInformationMessage();
			timeMessage.setFromAdress(this.config.vCubeAdress);
			timeMessage.setSequenceNumber(getNewSequnceNumber());
			timeMessage.setToAdress(pairMessage.getFromAdress());
			timeMessage.setTime(new Date());
			outMessages.add(timeMessage);

			// Setup valve
			MaxConfigValveMessage valve = new MaxConfigValveMessage();
			valve.setBoostDuration(config.boostDurationMins);
			valve.setBoostValvePosition(config.boostValvePositionPercent);
			DecalcEntry decalc = valve.new DecalcEntry();
			decalc.day = config.decalcDay;
			decalc.hour = config.decalcHour;
			valve.setDecalc(decalc);
			valve.setFromAdress(config.vCubeAdress);
			valve.setMaxValvePosition(config.maxValvePosition);
			valve.setSequenceNumber(getNewSequnceNumber());
			valve.setToAdress(pairMessage.getFromAdress());
			valve.setValveOffset(config.valveOffsetPercent);
			outMessages.add(valve);

			// Set Temperatures
			MaxConfigureTemperaturesMessage temps = new MaxConfigureTemperaturesMessage();
			temps.setComfortTemp(config.comfortTemperatur);
			temps.setEcoTemp(config.ecoTemperatur);
			temps.setFromAdress(config.vCubeAdress);
			temps.setMaxTemp(config.maxTemp);
			temps.setMinTemp(config.minTemp);
			temps.setOffsetTemp(((ThermostatConfiguration) config.devices.get(pairMessage.getFromAdress())).offset);
			temps.setSequenceNumber(getNewSequnceNumber());
			temps.setToAdress(pairMessage.getFromAdress());
			temps.setWindowOpenTemp(config.windowOpenTemperatur);
			temps.setWindowOpenTime(config.windowOpenTimeMins);
			outMessages.add(temps);

			// Setup temperatur
			MaxSetTemperatureMessage currentTemp = new MaxSetTemperatureMessage();
			currentTemp.setFromAdress(config.vCubeAdress);
			currentTemp.setTemp(config.setTemp);
			currentTemp.setTemporaryUntil(config.temporaryUntilDate);
			currentTemp.setMode(config.mode);
			currentTemp.setSequenceNumber(getNewSequnceNumber());
			currentTemp.setToAdress(pairMessage.getFromAdress());
			outMessages.add(currentTemp);

			// Setup weekly Profile
			createWeekProfile(pairMessage.getFromAdress(), config.currentWeekProfile);
		}

		AmbientControlMW.getRoom().qeueManager.putOutMessages(outMessages);

	}


	/**
	 * @param fromAdress
	 * @param weekProfile
	 */
	private void createWeekProfile(Integer deviceAdress, String weekProfile) {
		HashMap<MaxDayInWeek, List<DayEntry>> profiles = config.weekProfiles.get(weekProfile);
		for (Entry<MaxDayInWeek, List<DayEntry>> currentDayProfile : profiles.entrySet()) {
			int entryCountPartOne = currentDayProfile.getValue().size();
			boolean twoParts = false;
			List<Message> messages = new ArrayList<Message>();

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

			AmbientControlMW.getRoom().qeueManager.putOutMessages(messages);
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
			System.out
			.println("Climate Manager - handleResponseMessage(): Error! Got Timeout for Device: " + device.config.label);

		} else if (state == State.RETRIEVED_ANSWER && response instanceof MaxAckMessage
				&& ((MaxAckMessage) response).getAckType() == MaxAckType.ACK_INVALID_MESSAGE) {

			device.config.invalidArgument = true;
			System.out.println("Climate Manager - handleResponseMessage(): Device: Error! " + device.config.label
					+ " got invalid Arguments from ClimateManager!");
		}

		try {
			RoomConfigurationFactory.commitTransaction();
		} catch (IOException e) {
			e.printStackTrace();
		}

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
	}


	/**
	 * @param message
	 * @throws IOException
	 */
	private void handleShutterState(MaxShutterContactStateMessage message) throws IOException {
		RoomConfigurationFactory.beginTransaction();
		ShutterContact shutter = (ShutterContact) AmbientControlMW.getRoom().getMaxComponents().get(message.getFromAdress());
		shutter.config.batteryLow = message.isBatteryLow();
		shutter.isOpen = message.isOpen();
		shutter.config.lastUpdate = new Date(System.currentTimeMillis());
		RoomConfigurationFactory.commitTransaction();
	}


	/**
	 * @param message
	 * @throws IOException
	 */
	private void handleThermostatState(MaxThermostatStateMessage message) throws IOException {

		Thermostat thermostat = (Thermostat) AmbientControlMW.getRoom().getMaxComponents().get(message.getFromAdress());
		if (thermostat == null) {
			System.out.println("ClimateManager handleThermostatState(): got request from unknown device: adress="
					+ message.getFromAdress());
			return;
		}

		RoomConfigurationFactory.beginTransaction();
		modeTracker.before();

		thermostat.config.batteryLow = message.isBatteryLow();
		thermostat.isLocked = message.isLocked();
		thermostat.config.lastUpdate = new Date(System.currentTimeMillis());

		thermostat.temperatur = message.getActualTemp();

		config.mode = message.getMode();
		config.temporaryUntilDate = message.getTemporaryUntil();
		config.setTemp = message.getSetTemp();

		RoomConfigurationFactory.commitTransaction();
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();

		modeTracker.handleMode(message.getSetTemp(), message.getMode(), message.getTemporaryUntil());
	}


	public void setMode(float temp, MaxThermostateMode mode, Date until) throws IOException {
		RoomConfigurationFactory.beginTransaction();
		modeTracker.before();

		if (until != null && config.mode != MaxThermostateMode.TEMPORARY)
			throw new IllegalArgumentException("An until date may only be set in temporary mode.");

		config.mode = mode;
		config.setTemp = temp;
		config.temporaryUntilDate = until;

		List<Message> messages = new ArrayList<Message>();

		for (MaxComponent current : AmbientControlMW.getRoom().getMaxComponents().values()) {
			if (current instanceof Thermostat) {
				MaxSetTemperatureMessage outMessage = new MaxSetTemperatureMessage();
				outMessage.setFromAdress(config.vCubeAdress);

				outMessage.setTemp(config.setTemp);
				outMessage.setTemporaryUntil(config.temporaryUntilDate);
				outMessage.setMode(config.mode);
				outMessage.setSequenceNumber(getNewSequnceNumber());
				outMessage.setToAdress(((Thermostat) current).config.adress);
				messages.add(outMessage);
			}
		}

		AmbientControlMW.getRoom().qeueManager.putOutMessages(messages);

		RoomConfigurationFactory.commitTransaction();
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();

		modeTracker.handleMode(temp, mode, until);
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


	private void setTimeInfoToComponents() {
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
	 * org.ambientlight.process.eventmanager.IEventListener#handleEvent(org.
	 * ambientlight.process.events.EventConfiguration)
	 */
	@Override
	public void handleEvent(Event event) {
		TODO do this with a simple repeating scheduled task
		if (event instanceof AlarmEvent) {
			if (event.equals(this.alarmForTimeSetting)) {
				setTimeInfoToComponents();
				registerAlarmForTimeSetting();
			}
		}
	}


	private void registerAlarmForTimeSetting() {
		AmbientControlMW.getRoom().eventManager.register(this, alarmForTimeSetting);
	}
}
