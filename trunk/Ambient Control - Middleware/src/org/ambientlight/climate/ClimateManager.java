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
import java.util.Date;
import java.util.List;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.MessageListener;
import org.ambientlight.messages.QeueManager.State;
import org.ambientlight.messages.max.MaxAckMessage;
import org.ambientlight.messages.max.MaxAckType;
import org.ambientlight.messages.max.MaxMessage;
import org.ambientlight.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.messages.max.MaxShutterContactStateMessage;
import org.ambientlight.messages.max.MaxThermostatStateMessage;
import org.ambientlight.messages.max.MaxThermostateMode;
import org.ambientlight.messages.max.MaxTimeInformationMessage;
import org.ambientlight.process.eventmanager.IEventListener;
import org.ambientlight.process.events.AlarmEvent;
import org.ambientlight.process.events.Event;
import org.ambientlight.room.ClimateConfiguration;
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


	public ClimateManager() {
		setTimeInfoToComponents();
		registerAlarmForTimeSetting();
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
				RoomConfigurationFactory.beginTransaction();
				handleThermostatState((MaxThermostatStateMessage) message);
				RoomConfigurationFactory.commitTransaction();
			}

			if (message instanceof MaxShutterContactStateMessage) {
				RoomConfigurationFactory.beginTransaction();
				handleShutterState((MaxShutterContactStateMessage) message);
				RoomConfigurationFactory.commitTransaction();
			}

		} catch (IOException e) {
			e.printStackTrace();
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
		int adress = ((MaxMessage) request).getToAdress();
		MaxComponent device = AmbientControlMW.getRoom().getMaxComponents().get(adress);
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

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
		try {
			RoomConfigurationFactory.commitTransaction();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		thermostat.config.batteryLow = message.isBatteryLow();
		thermostat.isLocked = message.isLocked();
		thermostat.config.lastUpdate = new Date(System.currentTimeMillis());

		// TODO notify EventManager for new messured temperatur
		thermostat.temperatur = message.getActualTemp();

		config.mode = message.getMode();
		config.temporaryUntilDate = message.getTemporaryUntil();
		config.setTemp = message.getSetTemp();
		RoomConfigurationFactory.commitTransaction();
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
	}


	public void setTemperatur(float temp, Date until) throws IOException {
		RoomConfigurationFactory.beginTransaction();

		config.setTemp = temp;
		config.temporaryUntilDate = until;
		if (until != null) {
			config.mode = MaxThermostateMode.TEMPORARY;
		}

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
