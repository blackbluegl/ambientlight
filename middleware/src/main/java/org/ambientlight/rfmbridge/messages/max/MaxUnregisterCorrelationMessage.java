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
import org.ambientlight.rfmbridge.messages.UnRegisterCorrelatorMessage;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxUnregisterCorrelationMessage extends UnRegisterCorrelatorMessage {

	/**
	 * unregister a correlator in the rfm22-bridge. all messages from that device will be broadcasted from now on to all
	 * max-dispatcher clients that have an connection to the rfm22-bridge
	 * 
	 * @param dispatcherType
	 * @param correlator
	 */
	public MaxUnregisterCorrelationMessage(DispatcherType dispatcherType, int adress, int vCubeAdress) {
		super(dispatcherType, adress + "_" + vCubeAdress);
	}

}
