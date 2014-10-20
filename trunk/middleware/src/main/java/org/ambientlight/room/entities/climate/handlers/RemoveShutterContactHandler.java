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

import org.ambientlight.Persistence;
import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.messages.DispatcherType;
import org.ambientlight.config.room.entities.climate.ClimateManagerConfiguration;
import org.ambientlight.rfmbridge.Message;
import org.ambientlight.rfmbridge.QeueManager;
import org.ambientlight.rfmbridge.QeueManager.State;
import org.ambientlight.rfmbridge.messages.UnRegisterCorrelatorMessage;
import org.ambientlight.rfmbridge.messages.max.MaxFactoryResetMessage;
import org.ambientlight.rfmbridge.messages.max.MaxMessage;
import org.ambientlight.rfmbridge.messages.max.MaxUnregisterCorrelationMessage;
import org.ambientlight.rfmbridge.messages.max.WaitForShutterContactCondition;
import org.ambientlight.room.entities.climate.ClimateManager;
import org.ambientlight.room.entities.climate.ShutterContact;
import org.ambientlight.room.entities.climate.util.MaxMessageCreator;


/**
 * @author Florian Bornkessel
 * 
 */
public class RemoveShutterContactHandler implements MessageActionHandler {

	private enum ActionState {
		SETUP, REMOVE_FROM_MODELL, FINISHED
	};

	private ClimateManagerConfiguration config;

	private Persistence persistence;

	private CallBackManager callbackManager;

	private QeueManager queueManager;

	private ActionState actionState = ActionState.SETUP;

	private int sequenceNumberForDeletedDevice;
	private ShutterContact device;


	public RemoveShutterContactHandler(ClimateManager climateManager, ShutterContact device, CallBackManager callbackManager,
			QeueManager queueManager, Persistence persistence, ClimateManagerConfiguration config) {

		this.queueManager = queueManager;

		this.config = config;

		this.persistence = persistence;

		this.callbackManager = callbackManager;

		persistence.beginTransaction();

		this.device = device;

		// check if removal of shuttercontact changes the open window state of
		// the thermostates
		boolean isAWindowOpen = climateManager.isAWindowOpen();
		this.device.setOpen(false);
		boolean isAWindowNowOpen = climateManager.isAWindowOpen();
		if (isAWindowOpen != isAWindowNowOpen) {
			climateManager.sendWindowStateToThermostates(isAWindowNowOpen);
		}

		// Wait until shutterContact comes alive and remove it
		WaitForShutterContactCondition condition = new WaitForShutterContactCondition(device.getAdress(),
				config.vCubeAdress);
		MaxFactoryResetMessage resetDevice = new MaxMessageCreator(config).getFactoryResetMessageForDevice(device.getAdress());
		this.sequenceNumberForDeletedDevice = resetDevice.getSequenceNumber();
		queueManager.putOutMessage(resetDevice, condition);

		actionState = ActionState.REMOVE_FROM_MODELL;

		persistence.commitTransaction();

		if (isAWindowOpen != isAWindowNowOpen) {
			callbackManager.roomConfigurationChanged();
		}
	}


	private void handleRemoveFromModell() {

		// remove correlator
		UnRegisterCorrelatorMessage unRegisterCorelator = new MaxUnregisterCorrelationMessage(DispatcherType.MAX,
				device.getAdress(),
				config.vCubeAdress);
		queueManager.putOutMessage(unRegisterCorelator);

		// Remove from modell
		persistence.beginTransaction();
		config.devices.remove(device.getAdress());
		persistence.commitTransaction();

		this.actionState = ActionState.FINISHED;

		callbackManager.roomConfigurationChanged();
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
		if (((MaxMessage) message).getSequenceNumber() == this.sequenceNumberForDeletedDevice) {
			System.out.println("RemoveShutterHandler onMessage(): The shutterContact responded now."
					+ " Please reset the contact manually.");
		}

		// catch all window shutter messages until it was reset - the climate
		// manager shall not handle any message during removal
		if (((MaxMessage) message).getFromAdress().equals(this.device.getAdress()) && this.actionState != ActionState.FINISHED)
			return true;

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
	public boolean onResponse(State state, Message response, Message request) {
		if (actionState == ActionState.REMOVE_FROM_MODELL
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
