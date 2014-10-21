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

import org.ambientlight.config.messages.DispatcherType;
import org.ambientlight.rfmbridge.messages.RegisterCorrelatorMessage;


/**
 * Message to register a correlator for the max dispatcher type in the rfm22-bridge. With that correlator the rfm22-bridge is able
 * to route messages from max components to the apropriate climate manager instance. Without an correlationt an incomming message
 * will be broadcasted to all max-dispatcher clients that are connected to the rfm22-bridge. Note this correlator is different
 * from the correlator in request messages. The message correlator maps response-messages to request messages. This correlator
 * binds devices to the climatemanager.
 * 
 * @author Florian Bornkessel
 */
public class MaxRegisterCorrelationMessage extends RegisterCorrelatorMessage {

	/**
	 * @param dispatcherType
	 * @param correlator
	 */
	public MaxRegisterCorrelationMessage(DispatcherType dispatcherType, int deviceAdress, int vCubeAdress) {
		super(dispatcherType, deviceAdress + "_" + vCubeAdress);
	}

}
