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

import java.util.Map;

import org.ambientlight.Persistence;
import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.messages.DispatcherType;
import org.ambientlight.config.room.entities.climate.ClimateManagerConfiguration;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.QeueManager;
import org.ambientlight.messages.QeueManager.State;
import org.ambientlight.messages.max.MaxFactoryResetMessage;
import org.ambientlight.messages.max.MaxRemoveLinkPartnerMessage;
import org.ambientlight.messages.max.MaxUnregisterCorrelationMessage;
import org.ambientlight.messages.rfm22bridge.UnRegisterCorrelatorMessage;
import org.ambientlight.room.entities.climate.MaxComponent;
import org.ambientlight.room.entities.climate.Thermostat;
import org.ambientlight.room.entities.climate.util.MaxMessageCreator;


/**
 * @author Florian Bornkessel
 * 
 */
public class RemoveThermostatHandler implements MessageActionHandler {

	boolean finished = false;


	public RemoveThermostatHandler(Thermostat device, Map<Integer, MaxComponent> devices, CallBackManager callbackManager,
			QeueManager queueManager, Persistence persistence, ClimateManagerConfiguration config) {

		persistence.beginTransaction();

		// unregister link from other thermostates
		for (MaxComponent currentDevice : devices.values()) {

			// only other thermostates
			if (currentDevice.getAdress() == device.getAdress() || currentDevice instanceof Thermostat == false) {
				continue;
			}

			MaxRemoveLinkPartnerMessage unlink = new MaxMessageCreator(config).getUnlinkMessageForDevice(
					currentDevice.getAdress(), device.getAdress(), device.getDeviceType());
			queueManager.putOutMessage(unlink);
		}

		// send remove
		MaxFactoryResetMessage resetDevice = new MaxMessageCreator(config).getFactoryResetMessageForDevice(device.getAdress());
		queueManager.putOutMessage(resetDevice);

		// remove correlator - rfm bridge does route its messages to all clients
		UnRegisterCorrelatorMessage unRegisterCorelator = new MaxUnregisterCorrelationMessage(DispatcherType.MAX,
				device.getAdress(),
				config.vCubeAdress);
		queueManager.putOutMessage(unRegisterCorelator);

		// Remove from modell
		devices.remove(device.getAdress());

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
		// there should occour no timeout
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
		// there should occour no timeout
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
