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

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.room.entities.climate.MaxComponent;
import org.ambientlight.config.room.entities.climate.Thermostat;
import org.ambientlight.messages.DispatcherType;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.QeueManager.State;
import org.ambientlight.messages.max.MaxFactoryResetMessage;
import org.ambientlight.messages.max.MaxRemoveLinkPartnerMessage;
import org.ambientlight.messages.max.MaxUnregisterCorrelationMessage;
import org.ambientlight.messages.rfm22bridge.UnRegisterCorrelatorMessage;
import org.ambientlight.room.Persistence;
import org.ambientlight.room.entities.climate.util.MaxMessageCreator;


/**
 * @author Florian Bornkessel
 * 
 */
public class RemoveThermostatHandler implements MessageActionHandler {

	boolean finished = false;


	public RemoveThermostatHandler(Thermostat device) {


		Persistence.beginTransaction();

		// unregister link from other thermostates
		for (MaxComponent currentDevice : AmbientControlMW.getRoom().climateManager.config.devices.values()) {

			// only other thermostates
			if (currentDevice.adress == device.adress || currentDevice instanceof Thermostat == false) {
				continue;
			}

			MaxRemoveLinkPartnerMessage unlink = MaxMessageCreator.getUnlinkMessageForDevice(currentDevice.adress, device.adress,
					device.getDeviceType());
			AmbientControlMW.getRoom().qeueManager.putOutMessage(unlink);
		}

		// send remove
		MaxFactoryResetMessage resetDevice = MaxMessageCreator.getFactoryResetMessageForDevice(device.adress);
		AmbientControlMW.getRoom().qeueManager.putOutMessage(resetDevice);

		// remove correlator - rfm bridge does route its messages to all clients
		UnRegisterCorrelatorMessage unRegisterCorelator = new MaxUnregisterCorrelationMessage(DispatcherType.MAX, device.adress,
				AmbientControlMW.getRoom().config.climateManager.vCubeAdress);
		AmbientControlMW.getRoom().qeueManager.putOutMessage(unRegisterCorelator);

		// Remove from modell
		AmbientControlMW.getRoom().climateManager.config.devices.remove(device.adress);

		Persistence.commitTransaction();
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
