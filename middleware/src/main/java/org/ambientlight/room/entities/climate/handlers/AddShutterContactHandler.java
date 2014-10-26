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
import org.ambientlight.rfmbridge.messages.max.MaxPairPingMessage;
import org.ambientlight.rfmbridge.messages.max.MaxPairPongMessage;
import org.ambientlight.rfmbridge.messages.max.MaxRegisterCorrelationMessage;
import org.ambientlight.room.entities.climate.ShutterContact;


/**
 * @author Florian Bornkessel
 * 
 */
public class AddShutterContactHandler implements MessageActionHandler {

	// will be called by climateManager at any time. Set to true if the handler is ready to be removed
	private boolean isFinished = false;


	public AddShutterContactHandler(MaxPairPingMessage pairMessage, ClimateManagerConfiguration config, QeueManager qeueManager,
			CallBackManager callbackManager, Persistence persistence) {

		persistence.beginTransaction();

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

		persistence.commitTransaction();

		// setup new device
		List<Message> outMessages = new ArrayList<Message>();

		// link shutter with us
		MaxPairPongMessage pairPong = new MaxPairPongMessage();
		pairPong.setFromAdress(config.vCubeAdress);
		pairPong.setToAdress(pairMessage.getFromAdress());
		pairPong.setSequenceNumber(pairMessage.getSequenceNumber());
		outMessages.add(pairPong);

		// register correlator at the rfmbridge. The rfmbridge will handle ack messages directly. Cause is: the shuttercontact
		// waits only 50msec. Via network is to slow.
		outMessages.add(new MaxRegisterCorrelationMessage(DispatcherType.MAX, pairMessage.getFromAdress(), config.vCubeAdress));

		// should be done alread. TODO: remove this code after a few tests
		// link devices
		// for (MaxComponent currentConfig : config.devices.values()) {
		//
		// // do link only with thermostates
		// if (currentConfig instanceof Thermostat == false) {
		// continue;
		// }
		//
		//
		// link current device to the proxy adress of the shutter
		// MaxAddLinkPartnerMessage linkCurrentToNew = new MaxMessageCreator(config).getLinkMessage(currentConfig.getAdress(),
		// config.proxyShutterContactAdress, DeviceType.SHUTTER_CONTACT);
		// outMessages.add(linkCurrentToNew);
		// }

		qeueManager.putOutMessages(outMessages);

		callbackManager.roomConfigurationChanged();

		isFinished = true;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.climate.MessageActionHandler#onMessage(org.ambientlight .messages.Message)
	 */
	@Override
	public boolean onMessage(Message message) {
		// we do not handle any unknown message here
		return false;
	}


	/*
	 * does not handle responses. The shuttercontact has asked us and waited for our response. The queuemanager repeats all send
	 * messages several times until an ack arrives. This should be fine most of the time.
	 * 
	 * @see org.ambientlight.climate.MessageActionHandler#onAckResponseMessage(org .ambientlight.messages.QeueManager.State,
	 * org.ambientlight.messages.Message, org.ambientlight.messages.Message)
	 */
	@Override
	public boolean onResponse(State state, Message response, Message request) {
		return false;
	}


	/*
	 * The handler is finished directly after it has initialized the shutter contact in the constructor
	 * 
	 * @see org.ambientlight.climate.MessageActionHandler#isFinished()
	 */
	@Override
	public boolean isFinished() {
		return isFinished;
	}

}
