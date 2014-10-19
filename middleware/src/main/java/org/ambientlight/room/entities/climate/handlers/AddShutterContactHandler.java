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
import org.ambientlight.messages.Message;
import org.ambientlight.messages.QeueManager;
import org.ambientlight.messages.QeueManager.State;
import org.ambientlight.messages.max.MaxAddLinkPartnerMessage;
import org.ambientlight.messages.max.MaxPairPingMessage;
import org.ambientlight.messages.max.MaxPairPongMessage;
import org.ambientlight.messages.max.MaxRegisterCorrelationMessage;
import org.ambientlight.room.entities.climate.MaxComponent;
import org.ambientlight.room.entities.climate.ShutterContact;
import org.ambientlight.room.entities.climate.Thermostat;
import org.ambientlight.room.entities.climate.util.DeviceType;
import org.ambientlight.room.entities.climate.util.MaxMessageCreator;


/**
 * @author Florian Bornkessel
 * 
 */
public class AddShutterContactHandler implements MessageActionHandler {

	boolean finished = false;


	public AddShutterContactHandler(MaxPairPingMessage pairMessage, ClimateManagerConfiguration config,
			QeueManager qeueManager,
			CallBackManager callbackManager, Persistence persistence) {

		// Send pair pong
		MaxPairPongMessage pairPong = new MaxPairPongMessage();
		pairPong.setFromAdress(config.vCubeAdress);
		pairPong.setToAdress(pairMessage.getFromAdress());

		pairPong.setSequenceNumber(pairMessage.getSequenceNumber());
		qeueManager.putOutMessage(pairPong);

		// add and setup new device
		persistence.beginTransaction();
		List<Message> outMessages = new ArrayList<Message>();

		// register the device at the rfmbridge - it will ack some requests
		// directly because the way over network is to long.
		outMessages.add(new MaxRegisterCorrelationMessage(DispatcherType.MAX, pairMessage.getFromAdress(), config.vCubeAdress));

		// create device
		ShutterContact device = new ShutterContact();

		device.setOpen(false);
		device.setLabel("Fensterkontakt");
		device.setAdress(pairMessage.getFromAdress());
		device.setBatteryLow(false);
		device.setFirmware(pairMessage.getFirmware());
		device.setInvalidArgument(false);
		device.setLastUpdate(new Date());
		device.setRfError(false);
		device.setSerial(pairMessage.getSerial());
		device.setTimedOut(false);

		config.devices.put(device.getAdress(), device);

		// link devices
		for (MaxComponent currentConfig : config.devices.values()) {

			// do link only with thermostates
			if (currentConfig instanceof Thermostat == false) {
				continue;
			}

			// link current device to the proxy adress of the shutter
			MaxAddLinkPartnerMessage linkCurrentToNew = new MaxMessageCreator(config).getLinkMessage(currentConfig.getAdress(),
					config.proxyShutterContactAdress, DeviceType.SHUTTER_CONTACT);
			outMessages.add(linkCurrentToNew);
		}

		qeueManager.putOutMessages(outMessages);
		persistence.commitTransaction();
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
		// we do not handle any unknown message here
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
		// we do not wait for an ack here. the queue retry mechanism should be
		// fine.
		return false;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.climate.MessageActionHandler#isFinished()
	 */
	@Override
	public boolean isFinished() {
		return this.finished;
	}

}
