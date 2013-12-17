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
import org.ambientlight.messages.DispatcherType;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.QeueManager.State;
import org.ambientlight.messages.max.MaxAddLinkPartnerMessage;
import org.ambientlight.messages.max.MaxPairPingMessage;
import org.ambientlight.messages.max.MaxPairPongMessage;
import org.ambientlight.messages.max.MaxRegisterCorrelationMessage;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.entities.Thermostat;


/**
 * @author Florian Bornkessel
 * 
 */
public class AddThermostateHandler implements MessageActionHandler {

	boolean finished = false;


	public AddThermostateHandler(MaxPairPingMessage pairMessage) {

		// Send pair pong
		MaxPairPongMessage pairPong = new MaxPairPongMessage();
		pairPong.setFromAdress(AmbientControlMW.getRoom().config.climate.vCubeAdress);
		pairPong.setToAdress(pairMessage.getFromAdress());

		pairPong.setSequenceNumber(pairMessage.getSequenceNumber());
		AmbientControlMW.getRoom().qeueManager.putOutMessage(pairPong);

		// add and setup new device
		RoomConfigurationFactory.beginTransaction();
		List<Message> outMessages = new ArrayList<Message>();

		// register the device at the rfmbridge - for direct routing
		outMessages.add(new MaxRegisterCorrelationMessage(DispatcherType.MAX, pairMessage.getFromAdress(), AmbientControlMW
				.getRoom().config.climate.vCubeAdress));

		// create device in ambientcontrol
		ThermostatConfiguration config = new ThermostatConfiguration();
		Thermostat device = new Thermostat();
		device.config = config;

		// it is the sensor value we can read. but we need something to
		// start
		device.temperatur = AmbientControlMW.getRoom().config.climate.setTemp;

		config.offset = AmbientControlMW.getRoom().config.climate.DEFAULT_OFFSET;
		config.label = "Thermostat";
		config.adress = pairMessage.getFromAdress();
		config.batteryLow = false;
		config.firmware = pairMessage.getFirmware();
		config.invalidArgument = false;
		config.lastUpdate = new Date();
		config.rfError = false;
		config.serial = pairMessage.getSerial();
		config.timedOut = false;

		// add device to ambientcontrol
		AmbientControlMW.getRoom().getMaxComponents().put(config.adress, device);
		AmbientControlMW.getRoom().config.climate.devices.put(config.adress, config);

		// setup Time;
		outMessages.add(MaxMessageCreator.getTimeInfoForDevice(new Date(), pairMessage.getFromAdress()));

		// setup valve
		outMessages.add(MaxMessageCreator.getConfigValveForDevice(pairMessage.getFromAdress()));

		// set Temperatures
		outMessages.add(MaxMessageCreator.getConfigureTemperatures(pairMessage.getFromAdress()));

		// setup temperatur
		outMessages.add(MaxMessageCreator.getSetTempForDevice(pairMessage.getFromAdress()));

		// set group
		outMessages.add(MaxMessageCreator.getSetGroupIdForDevice(pairMessage.getFromAdress()));

		// setup weekly Profile
		List<Message> weekProfile = MaxMessageCreator.getWeekProfileForDevice(pairMessage.getFromAdress(),
				AmbientControlMW.getRoom().config.climate.currentWeekProfile);
		for (Message dayProfile : weekProfile) {
			outMessages.add(dayProfile);
		}

		// link devices
		for (MaxComponentConfiguration currentConfig : AmbientControlMW.getRoom().config.climate.devices.values()) {

			// do not link with ourself
			if (currentConfig.adress == pairMessage.getFromAdress()) {
				continue;
			}

			if (currentConfig instanceof ThermostatConfiguration) {
				// link current to new
				MaxAddLinkPartnerMessage linkCurrentToNew = MaxMessageCreator.getLinkMessage(currentConfig.adress,
						pairMessage.getFromAdress(), pairMessage.getDeviceType());
				outMessages.add(linkCurrentToNew);

				// link new device to current
				MaxAddLinkPartnerMessage linkNewToCurrent = MaxMessageCreator.getLinkMessage(pairMessage.getFromAdress(),
						currentConfig.adress, currentConfig.getDeviceType());
				outMessages.add(linkNewToCurrent);
			}

			if (currentConfig instanceof ShutterContactConfiguration) {
				// link new device to current
				MaxAddLinkPartnerMessage linkNewToCurrent = MaxMessageCreator.getLinkMessage(pairMessage.getFromAdress(),
						((ShutterContactConfiguration) currentConfig).proxyAdress, currentConfig.getDeviceType());
				outMessages.add(linkNewToCurrent);
			}
		}

		AmbientControlMW.getRoom().qeueManager.putOutMessages(outMessages);
		RoomConfigurationFactory.commitTransaction();
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();

		finished = true;
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
		// we do not manage any incomming messages for this action
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
	public boolean onResponse(State state, Message response, Message request) {
		// normally the thermostates react within the retry amount of the
		// requests.
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
