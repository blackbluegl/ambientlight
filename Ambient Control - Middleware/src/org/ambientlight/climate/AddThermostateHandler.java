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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.room.actors.MaxComponentConfiguration;
import org.ambientlight.config.room.actors.ShutterContactConfiguration;
import org.ambientlight.config.room.actors.ThermostatConfiguration;
import org.ambientlight.messages.ConditionalMessage;
import org.ambientlight.messages.DispatcherType;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.QeueManager.State;
import org.ambientlight.messages.max.DeviceType;
import org.ambientlight.messages.max.MaxAddLinkPartnerMessage;
import org.ambientlight.messages.max.MaxPairPingMessage;
import org.ambientlight.messages.max.MaxPairPongMessage;
import org.ambientlight.messages.max.MaxRegisterCorrelationMessage;
import org.ambientlight.messages.max.WaitForShutterContactCondition;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.entities.MaxComponent;
import org.ambientlight.room.entities.ShutterContact;
import org.ambientlight.room.entities.Thermostat;


/**
 * @author Florian Bornkessel
 * 
 */
public class AddThermostateHandler implements MessageActionHandler {

	boolean finished = false;


	public AddThermostateHandler(MaxPairPingMessage pairMessage, boolean newDevice) {

		// check for known deviceTypes
		if (pairMessage.getDeviceType() == null && pairMessage.getDeviceType() != DeviceType.HEATING_THERMOSTAT
				&& pairMessage.getDeviceType() != DeviceType.HEATING_THERMOSTAT_PLUS
				&& pairMessage.getDeviceType() != DeviceType.SHUTTER_CONTACT) {
			System.out.println("ClimateManager - sendPong(): We do not support this device");
			return;
		}

		// Send pair pong
		MaxPairPongMessage pairPong = new MaxPairPongMessage();
		pairPong.setFromAdress(AmbientControlMW.getRoom().config.climate.vCubeAdress);
		pairPong.setToAdress(pairMessage.getFromAdress());

		pairPong.setSequenceNumber(pairMessage.getSequenceNumber());
		AmbientControlMW.getRoom().qeueManager.putOutMessage(pairPong);

		if (newDevice) {

			// add and setup new device
			RoomConfigurationFactory.beginTransaction();
			List<ConditionalMessage> outMessages = new ArrayList<ConditionalMessage>();

			// register the device at the rfmbridge - it will ack some requests
			// directly because the way over network is to long. eg.
			// shuttercontact messages.
			outMessages.add(new ConditionalMessage(null, new MaxRegisterCorrelationMessage(DispatcherType.MAX, pairMessage
					.getFromAdress(), AmbientControlMW.getRoom().config.climate.vCubeAdress)));

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
				((Thermostat) device).temperatur = AmbientControlMW.getRoom().config.climate.setTemp;

				((ThermostatConfiguration) config).offset = AmbientControlMW.getRoom().config.climate.DEFAULT_OFFSET;
				config.label = "Thermostat";

				// Setup Time;
				outMessages.add(new ConditionalMessage(null, MaxMessageCreator.getTimeInfoForDevice(new Date(),
						pairMessage.getFromAdress())));

				// Setup valve
				outMessages.add(new ConditionalMessage(null, MaxMessageCreator.getConfigValveForDevice(pairMessage
						.getFromAdress())));

				// Set Temperatures
				outMessages.add(new ConditionalMessage(null, MaxMessageCreator.getConfigureTemperatures(pairMessage
						.getFromAdress())));

				// Setup temperatur
				outMessages.add(new ConditionalMessage(null, MaxMessageCreator.getSetTempForDevice(pairMessage.getFromAdress())));

				// Set group
				outMessages.add(new ConditionalMessage(null,
						MaxMessageCreator.getSetGroupIdForDevice(pairMessage.getFromAdress())));

				// Setup weekly Profile
				List<Message> weekProfile = MaxMessageCreator.getWeekProfileForDevice(pairMessage.getFromAdress(),
						AmbientControlMW.getRoom().config.climate.currentWeekProfile);
				for (Message dayProfile : weekProfile) {
					outMessages.add(new ConditionalMessage(null, dayProfile));
				}

			} else if (pairMessage.getDeviceType() == DeviceType.SHUTTER_CONTACT) {
				device = new ShutterContact();
				config = new ShutterContactConfiguration();
				device.config = config;
				((ShutterContact) device).isOpen = false;
				config.label = "Fensterkontakt";
				((ShutterContactConfiguration) config).proxyAdress = config.adress + 1;
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

			DeviceType newDeviceType = device.config.getDeviceType();
			int newAdress = device.config.adress;
			if (device instanceof ShutterContact) {
				newAdress = ((ShutterContactConfiguration) device.config).proxyAdress;
			}

			// link devices
			for (MaxComponentConfiguration currentConfig : AmbientControlMW.getRoom().config.climate.devices.values()) {

				// do not link with ourself and only with thermostates
				if (currentConfig.adress == pairMessage.getFromAdress()
						&& currentConfig instanceof ThermostatConfiguration == false) {
					continue;
				}

				// link new device to current
				MaxAddLinkPartnerMessage linkCurrentToNew = MaxMessageCreator.getLinkMessage(currentConfig.adress,
						pairMessage.getFromAdress(), pairMessage.getDeviceType());
				outMessages.add(new ConditionalMessage(conditionForCurrent, linkCurrentToNew));

				// link the current to the new device - the device is pairing
				// and woken up
				// no condition is needed
				MaxAddLinkPartnerMessage linkNewToCurrent = MaxMessageCreator.getLinkMessage(pairMessage.getFromAdress(),
						currentConfig.adress, currentConfig.getDeviceType());
				outMessages.add(new ConditionalMessage(null, linkNewToCurrent));
			}

			AmbientControlMW.getRoom().qeueManager.putOutMessagesWithCondition(outMessages);
			RoomConfigurationFactory.commitTransaction();
			AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();

			finished = true;
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.climate.MessageActionHandler#onMessage(org.ambientlight
	 * .messages.Message)
	 */
	@Override
	public boolean onMessage(Message message) {
		// TODO Auto-generated method stub
		return false;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.climate.MessageActionHandler#onAckResponseMessage(org
	 * .ambientlight.messages.QeueManager.State,
	 * org.ambientlight.messages.Message, org.ambientlight.messages.Message)
	 */
	@Override
	public boolean onAckResponseMessage(State state, Message response, Message request) {
		// TODO Auto-generated method stub
		return false;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.climate.MessageActionHandler#isFinished()
	 */
	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return this.finished;
	}

}
