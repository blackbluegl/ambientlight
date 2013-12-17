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
import org.ambientlight.messages.max.DeviceType;
import org.ambientlight.messages.max.MaxAddLinkPartnerMessage;
import org.ambientlight.messages.max.MaxPairPingMessage;
import org.ambientlight.messages.max.MaxPairPongMessage;
import org.ambientlight.messages.max.MaxRegisterCorrelationMessage;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.entities.ShutterContact;


/**
 * @author Florian Bornkessel
 * 
 */
public class AddShutterContactHandler implements MessageActionHandler {

	boolean finished = false;


	public AddShutterContactHandler(MaxPairPingMessage pairMessage) {

		// Send pair pong
		MaxPairPongMessage pairPong = new MaxPairPongMessage();
		pairPong.setFromAdress(AmbientControlMW.getRoom().config.climate.vCubeAdress);
		pairPong.setToAdress(pairMessage.getFromAdress());

		pairPong.setSequenceNumber(pairMessage.getSequenceNumber());
		AmbientControlMW.getRoom().qeueManager.putOutMessage(pairPong);

		// add and setup new device
		RoomConfigurationFactory.beginTransaction();
		List<Message> outMessages = new ArrayList<Message>();

		// register the device at the rfmbridge - it will ack some requests
		// directly because the way over network is to long.
		outMessages.add(new MaxRegisterCorrelationMessage(DispatcherType.MAX, pairMessage.getFromAdress(), AmbientControlMW
				.getRoom().config.climate.vCubeAdress));

		// create device
		ShutterContactConfiguration config = new ShutterContactConfiguration();
		ShutterContact device = new ShutterContact();
		device.config = config;

		device.isOpen = false;

		config.label = "Fensterkontakt";
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

		// link devices
		for (MaxComponentConfiguration currentConfig : AmbientControlMW.getRoom().config.climate.devices.values()) {

			// do link only with thermostates
			if (currentConfig instanceof ThermostatConfiguration == false) {
				continue;
			}

			// link current device to the proxy adress of the shutter
			MaxAddLinkPartnerMessage linkCurrentToNew = MaxMessageCreator.getLinkMessage(currentConfig.adress,
					AmbientControlMW.getRoom().config.climate.proxyShutterContactAdress, DeviceType.SHUTTER_CONTACT);
			outMessages.add(linkCurrentToNew);
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
