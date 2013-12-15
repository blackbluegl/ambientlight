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

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.room.actors.MaxComponentConfiguration;
import org.ambientlight.config.room.actors.ThermostatConfiguration;
import org.ambientlight.messages.DispatcherType;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.QeueManager.State;
import org.ambientlight.messages.max.MaxFactoryResetMessage;
import org.ambientlight.messages.max.MaxRemoveLinkPartnerMessage;
import org.ambientlight.messages.max.MaxUnregisterCorrelationMessage;
import org.ambientlight.messages.rfm22bridge.UnRegisterCorrelatorMessage;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.entities.Thermostat;


/**
 * @author Florian Bornkessel
 * 
 */
public class RemoveThermostatHandler implements MessageActionHandler {

	boolean finished = false;


	public RemoveThermostatHandler(Thermostat device) {

		ThermostatConfiguration config = (ThermostatConfiguration) device.config;

		RoomConfigurationFactory.beginTransaction();

		// unregister link from other thermostates
		for (MaxComponentConfiguration currentConfig : AmbientControlMW.getRoom().climateManager.config.devices.values()) {

			// only other thermostates
			if (currentConfig.adress == config.adress || currentConfig instanceof ThermostatConfiguration == false) {
				continue;
			}

			MaxRemoveLinkPartnerMessage unlink = MaxMessageCreator.getUnlinkMessageForDevice(currentConfig.adress, config.adress,
					config.getDeviceType());
			AmbientControlMW.getRoom().qeueManager.putOutMessage(unlink);
		}

		// send remove
		MaxFactoryResetMessage resetDevice = MaxMessageCreator.getFactoryResetMessageForDevice(device.config.adress);
		AmbientControlMW.getRoom().qeueManager.putOutMessage(resetDevice);

		// remove correlator - rfm bridge does route its messages to all clients
		UnRegisterCorrelatorMessage unRegisterCorelator = new MaxUnregisterCorrelationMessage(DispatcherType.MAX, config.adress,
				AmbientControlMW.getRoom().config.climate.vCubeAdress);
		AmbientControlMW.getRoom().qeueManager.putOutMessage(unRegisterCorelator);

		// Remove from modell
		AmbientControlMW.getRoom().getMaxComponents().remove(config.adress);
		AmbientControlMW.getRoom().climateManager.config.devices.remove(config.adress);

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
	public boolean onAckResponseMessage(State state, Message response, Message request) {
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
