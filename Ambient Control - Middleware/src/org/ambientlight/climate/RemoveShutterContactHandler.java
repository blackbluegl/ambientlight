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

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.room.actors.MaxComponentConfiguration;
import org.ambientlight.config.room.actors.ShutterContactConfiguration;
import org.ambientlight.config.room.actors.ThermostatConfiguration;
import org.ambientlight.messages.DispatcherType;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.QeueManager.State;
import org.ambientlight.messages.max.MaxFactoryResetMessage;
import org.ambientlight.messages.max.MaxMessage;
import org.ambientlight.messages.max.MaxRemoveLinkPartnerMessage;
import org.ambientlight.messages.max.MaxUnregisterCorrelationMessage;
import org.ambientlight.messages.max.WaitForShutterContactCondition;
import org.ambientlight.messages.rfm22bridge.UnRegisterCorrelatorMessage;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.entities.ShutterContact;


/**
 * @author Florian Bornkessel
 * 
 */
public class RemoveShutterContactHandler implements MessageActionHandler {

	private enum ActionState {
		SETUP, REMOVE_FROM_MODELL, FINISHED
	};

	private ActionState actionState = ActionState.SETUP;

	private int sequenceNumberForDeletedDevice;
	private ShutterContact device;


	public RemoveShutterContactHandler(ShutterContact device) {
		this.device = device;
		ShutterContactConfiguration config = (ShutterContactConfiguration) device.config;

		ArrayList<Integer> removedLinks = new ArrayList<Integer>();

		// unlink thermostates
		for (MaxComponentConfiguration currentConfig : AmbientControlMW.getRoom().config.climate.devices.values()) {

			if (currentConfig instanceof ThermostatConfiguration == false) {
				continue;
			}

			MaxRemoveLinkPartnerMessage unlink = MaxMessageCreator.getUnlinkMessageForDevice(currentConfig.adress,
					config.proxyAdress, device.config.getDeviceType());
			AmbientControlMW.getRoom().qeueManager.putOutMessage(unlink);
			removedLinks.add(currentConfig.adress);
		}

		// Wait until shutterContact comes alive and remove it
		WaitForShutterContactCondition condition = new WaitForShutterContactCondition(device.config.adress,
				AmbientControlMW.getRoom().config.climate.vCubeAdress);
		MaxFactoryResetMessage resetDevice = MaxMessageCreator.getFactoryResetMessageForDevice(device.config.adress);
		this.sequenceNumberForDeletedDevice = resetDevice.getSequenceNumber();
		AmbientControlMW.getRoom().qeueManager.putOutMessage(resetDevice, condition);

		actionState = ActionState.REMOVE_FROM_MODELL;
	}


	private void handleRemoveFromModell() {

		// remove correlator
		UnRegisterCorrelatorMessage unRegisterCorelator = new MaxUnregisterCorrelationMessage(DispatcherType.MAX,
				device.config.adress, AmbientControlMW.getRoom().config.climate.vCubeAdress);
		AmbientControlMW.getRoom().qeueManager.putOutMessage(unRegisterCorelator);

		// Remove from modell
		RoomConfigurationFactory.beginTransaction();
		AmbientControlMW.getRoom().getMaxComponents().remove(device.config.adress);
		AmbientControlMW.getRoom().config.climate.devices.remove(device.config.adress);
		RoomConfigurationFactory.commitTransaction();

		this.actionState = ActionState.FINISHED;

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.messages.MessageListener#onMessage(org.ambientlight.
	 * messages.Message)
	 */
	@Override
	public boolean onMessage(Message message) {
		// we are not interested in messages without a context
		if (message instanceof MaxMessage && ((MaxMessage) message).getSequenceNumber() == this.sequenceNumberForDeletedDevice) {
			System.out.println("RemoveShutterHandler onMessage(): The shutterContact responded now."
					+ " Please reset the contact manually.");
		}
		return false;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.MessageListener#onAckResponseMessage(org.
	 * ambientlight.messages.QeueManager.State,
	 * org.ambientlight.messages.Message, org.ambientlight.messages.Message)
	 */
	@Override
	public boolean onAckResponseMessage(State state, Message response, Message request) {
		if (actionState == ActionState.REMOVE_FROM_MODELL && request instanceof MaxMessage
				&& ((MaxMessage) request).getSequenceNumber() == this.sequenceNumberForDeletedDevice) {

			if (state == State.TIMED_OUT) {
				System.out.println("RemoveShutterHandler onAckResponseMessage(): got timeout."
						+ " Please reset the contact manually");
			} else if (state == State.RETRIEVED_ANSWER) {
				System.out.println("RemoveShutterHandler onAckResponseMessage(): successfully removed shutterContact.");
			}

			this.handleRemoveFromModell();
			return true;
		}
		return false;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.climate.MessageActionHandler#isFinished()
	 */
	@Override
	public boolean isFinished() {
		if (actionState == ActionState.FINISHED)
			return true;
		return false;
	}
}
