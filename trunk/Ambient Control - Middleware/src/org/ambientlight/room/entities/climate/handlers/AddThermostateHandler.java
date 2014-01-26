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

package org.ambientlight.room.entities.climate.handlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.room.entities.climate.MaxComponent;
import org.ambientlight.config.room.entities.climate.Thermostat;
import org.ambientlight.messages.DispatcherType;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.QeueManager.State;
import org.ambientlight.messages.max.DeviceType;
import org.ambientlight.messages.max.MaxAddLinkPartnerMessage;
import org.ambientlight.messages.max.MaxPairPingMessage;
import org.ambientlight.messages.max.MaxPairPongMessage;
import org.ambientlight.messages.max.MaxRegisterCorrelationMessage;
import org.ambientlight.room.Persistence;
import org.ambientlight.room.entities.climate.util.MaxMessageCreator;


/**
 * @author Florian Bornkessel
 * 
 */
public class AddThermostateHandler implements MessageActionHandler {

	boolean finished = false;


	public AddThermostateHandler(MaxPairPingMessage pairMessage, CallBackManager callbackManager) {

		// Send pair pong
		MaxPairPongMessage pairPong = new MaxPairPongMessage();
		pairPong.setFromAdress(AmbientControlMW.getRoom().config.climateManager.vCubeAdress);
		pairPong.setToAdress(pairMessage.getFromAdress());

		pairPong.setSequenceNumber(pairMessage.getSequenceNumber());
		AmbientControlMW.getRoom().qeueManager.putOutMessage(pairPong);

		// add and setup new device
		Persistence.beginTransaction();
		List<Message> outMessages = new ArrayList<Message>();

		// register the device at the rfmbridge - for direct routing
		outMessages.add(new MaxRegisterCorrelationMessage(DispatcherType.MAX, pairMessage.getFromAdress(), AmbientControlMW
				.getRoom().config.climateManager.vCubeAdress));

		// create device in ambientcontrol
		Thermostat device = new Thermostat();

		// it is the sensor value we can read. but we need something to
		// start
		device.setTemperature(AmbientControlMW.getRoom().config.climateManager.setTemp);

		device.offset = AmbientControlMW.getRoom().config.climateManager.DEFAULT_OFFSET;
		device.label = "Thermostat";
		device.adress = pairMessage.getFromAdress();
		device.batteryLow = false;
		device.firmware = pairMessage.getFirmware();
		device.invalidArgument = false;
		device.lastUpdate = new Date();
		device.rfError = false;
		device.serial = pairMessage.getSerial();
		device.timedOut = false;

		// add device to ambientcontrol
		AmbientControlMW.getRoom().config.climateManager.devices.put(device.adress, device);

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
				AmbientControlMW.getRoom().config.climateManager.currentWeekProfile);
		for (Message dayProfile : weekProfile) {
			outMessages.add(dayProfile);
		}

		// link to proxyShutterContact
		MaxAddLinkPartnerMessage linkToShutterContact = MaxMessageCreator.getLinkMessage(device.adress,
				AmbientControlMW.getRoom().config.climateManager.proxyShutterContactAdress, DeviceType.SHUTTER_CONTACT);
		outMessages.add(linkToShutterContact);

		// link devices to other thermostates
		for (MaxComponent currentConfig : AmbientControlMW.getRoom().config.climateManager.devices.values()) {

			// do not link with ourself
			if (currentConfig.adress == pairMessage.getFromAdress()) {
				continue;
			}

			if (currentConfig instanceof Thermostat) {
				// link current to new
				MaxAddLinkPartnerMessage linkCurrentToNew = MaxMessageCreator.getLinkMessage(currentConfig.adress,
						pairMessage.getFromAdress(), pairMessage.getDeviceType());
				outMessages.add(linkCurrentToNew);

				// link new device to current
				MaxAddLinkPartnerMessage linkNewToCurrent = MaxMessageCreator.getLinkMessage(pairMessage.getFromAdress(),
						currentConfig.adress, currentConfig.getDeviceType());
				outMessages.add(linkNewToCurrent);
			}
		}

		AmbientControlMW.getRoom().qeueManager.putOutMessages(outMessages);
		Persistence.commitTransaction();
		callbackManager.roomConfigurationChanged();

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
