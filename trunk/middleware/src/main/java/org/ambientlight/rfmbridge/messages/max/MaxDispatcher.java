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

package org.ambientlight.rfmbridge.messages.max;

import java.io.IOException;

import org.ambientlight.config.messages.DispatcherConfiguration;
import org.ambientlight.config.messages.DispatcherType;
import org.ambientlight.rfmbridge.InDispatcher;
import org.ambientlight.rfmbridge.Message;
import org.ambientlight.rfmbridge.QeueManager;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxDispatcher extends InDispatcher {

	public MaxDispatcher(DispatcherConfiguration configuration, QeueManager queueManager) {
		super(configuration, queueManager);
	}


	@Override
	public DispatcherType getDispatcherType() {
		return DispatcherType.MAX;
	}


	@Override
	protected void deliverPayLoad(Message message) throws IOException {
		// write the binary message for the max devices
		if (message instanceof MaxMessage) {
			socket.getOutputStream().write(((MaxMessage) message).getPayload());
			socket.getOutputStream().flush();
		}
	}


	@Override
	public Message handleInMessage(String dispatcherType, byte[] input) {
		// handle max messages
		MaxMessage message = new MaxMessage();
		message.setPayload(input);

		MaxMessage result = new MaxMessage();
		switch (message.getMessageType()) {
		case THERMOSTAT_STATE:
			result = new MaxThermostatStateMessage();
			break;
		case SET_TEMPERATURE:
			result = new MaxSetTemperatureMessage();
			break;
		case ACK:
			result = new MaxAckMessage();
			break;
		case TIME_INFORMATION:
			result = new MaxTimeInformationMessage();
			break;
		case CONFIG_WEEK_PROFILE:
			result = new MaxConfigureWeekProgrammMessage();
			break;
		case CONFIG_VALVE:
			result = new MaxConfigValveMessage();
			break;
		case CONFIG_TEMPERATURES:
			result = new MaxConfigureTemperaturesMessage();
			break;
		case PAIR_PING:
			result = new MaxPairPingMessage();
			break;
		case PAIR_PONG:
			result = new MaxPairPongMessage();
			break;
		case SET_GROUP_ID:
			result = new MaxSetGroupIdMessage();
			break;
		case REMOVE_GROUP_ID:
			result = new MaxRemoveGroupIdMessage();
			break;
		case SHUTTER_CONTACT_STATE:
			result = new MaxShutterContactStateMessage();
			break;
		case WAKE_UP:
			result = new MaxWakeUpMessage();
			break;
		case RESET:
			result = new MaxFactoryResetMessage();
			break;
		case ADD_LINK_PARTNER:
			result = new MaxAddLinkPartnerMessage();
			break;
		case REMOVE_LINK_PARTNER:
			result = new MaxRemoveLinkPartnerMessage();
			break;
		default:
			result = new MaxMessage();
		}

		result.setPayload(input);

		return result;
	}

}
