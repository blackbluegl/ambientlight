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

import org.ambientlight.Persistence;
import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.messages.DispatcherType;
import org.ambientlight.config.room.entities.climate.ClimateManagerConfiguration;
import org.ambientlight.rfmbridge.Message;
import org.ambientlight.rfmbridge.QeueManager;
import org.ambientlight.rfmbridge.QeueManager.State;
import org.ambientlight.rfmbridge.messages.max.MaxAddLinkPartnerMessage;
import org.ambientlight.rfmbridge.messages.max.MaxPairPingMessage;
import org.ambientlight.rfmbridge.messages.max.MaxPairPongMessage;
import org.ambientlight.rfmbridge.messages.max.MaxRegisterCorrelationMessage;
import org.ambientlight.room.entities.climate.MaxComponent;
import org.ambientlight.room.entities.climate.Thermostat;
import org.ambientlight.room.entities.climate.util.DeviceType;
import org.ambientlight.room.entities.climate.util.MaxMessageCreator;
import org.ambientlight.room.entities.climate.util.MaxUtil;


/**
 * @author Florian Bornkessel
 * 
 */
public class AddThermostateHandler implements MessageActionHandler {

	// will be called by climateManager at any time. Set to true if the handler is ready to be removed
	private boolean isFinished = false;


	public AddThermostateHandler(MaxPairPingMessage pairMessage, CallBackManager callbackManager, QeueManager qeueManager,
			Persistence persistence, ClimateManagerConfiguration config) {

		// add new device
		persistence.beginTransaction();

		// create device in ambientcontrol
		Thermostat device = new Thermostat();
		// we need a value to start with. so use the current temperature of the room.
		device.setTemperature(config.temperature);
		device.setOffset(MaxUtil.DEFAULT_OFFSET);
		device.setLabel("Thermostat");
		device.setAdress(pairMessage.getFromAdress());
		device.setBatteryLow(false);
		device.setFirmware(pairMessage.getFirmware());
		device.setInvalidArgument(false);
		device.setLastUpdate(new Date());
		device.setRfError(false);
		device.setSerial(pairMessage.getSerial());
		device.setTimedOut(false);

		// add device to ambientcontrol
		config.devices.put(device.getAdress(), device);

		persistence.commitTransaction();

		// Setup device
		List<Message> outMessages = new ArrayList<Message>();

		// Send pair pong
		MaxPairPongMessage pairPong = new MaxPairPongMessage();
		pairPong.setFromAdress(config.vCubeAdress);
		pairPong.setToAdress(pairMessage.getFromAdress());
		pairPong.setSequenceNumber(pairMessage.getSequenceNumber());
		outMessages.add(pairPong);

		// register the device at the rfmbridge - for direct routing
		outMessages.add(new MaxRegisterCorrelationMessage(DispatcherType.MAX, pairMessage.getFromAdress(), config.vCubeAdress));

		// setup Time;
		outMessages.add(new MaxMessageCreator(config).getTimeInfoForDevice(new Date(), pairMessage.getFromAdress()));

		// setup valve
		outMessages.add(new MaxMessageCreator(config).getConfigValveForDevice(pairMessage.getFromAdress()));

		// set Temperatures
		outMessages.add(new MaxMessageCreator(config).getConfigureTemperatures(pairMessage.getFromAdress()));

		// setup temperatur
		outMessages.add(new MaxMessageCreator(config).getSetTempForDevice(pairMessage.getFromAdress()));

		// set group
		outMessages.add(new MaxMessageCreator(config).getSetGroupIdForDevice(pairMessage.getFromAdress()));

		// setup weekly Profile
		List<Message> weekProfile = new MaxMessageCreator(config).getWeekProfileForDevice(pairMessage.getFromAdress(),
				config.currentWeekProfile);
		for (Message dayProfile : weekProfile) {
			outMessages.add(dayProfile);
		}

		// link to proxyShutterContact
		MaxAddLinkPartnerMessage linkToShutterContact = new MaxMessageCreator(config).getLinkMessage(device.getAdress(),
				config.proxyShutterContactAdress, DeviceType.SHUTTER_CONTACT);
		outMessages.add(linkToShutterContact);

		// link devices to other thermostates
		for (MaxComponent currentConfig : config.devices.values()) {

			// do not link with itself
			if (currentConfig.getAdress() == pairMessage.getFromAdress()) {
				continue;
			}

			if (currentConfig instanceof Thermostat) {
				// link current to new
				MaxAddLinkPartnerMessage linkCurrentToNew = new MaxMessageCreator(config).getLinkMessage(
						currentConfig.getAdress(), pairMessage.getFromAdress(), pairMessage.getDeviceType());
				outMessages.add(linkCurrentToNew);

				// link new device to current
				MaxAddLinkPartnerMessage linkNewToCurrent = new MaxMessageCreator(config).getLinkMessage(
						pairMessage.getFromAdress(), currentConfig.getAdress(), currentConfig.getDeviceType());
				outMessages.add(linkNewToCurrent);
			}
		}

		qeueManager.putOutMessages(outMessages);

		callbackManager.roomConfigurationChanged();

		isFinished = true;
	}


	/*
	 * we do not manage any incomming messages for this action
	 * 
	 * @see org.ambientlight.climate.MessageActionHandler#onMessage(org.ambientlight .messages.Message)
	 */
	@Override
	public boolean onMessage(Message message) {
		return false;
	}


	/*
	 * normally the thermostates react within the retry amount of the // requests.
	 * 
	 * @see org.ambientlight.climate.MessageActionHandler#onAckResponseMessage(org .ambientlight.messages.QeueManager.State,
	 * org.ambientlight.messages.Message, org.ambientlight.messages.Message)
	 */
	@Override
	public boolean onResponse(State state, Message response, Message request) {
		return false;
	}


	/*
	 * always finisched after frist run.
	 * 
	 * @see org.ambientlight.climate.MessageActionHandler#isFinished()
	 */
	@Override
	public boolean isFinished() {
		return isFinished;
	}

}
