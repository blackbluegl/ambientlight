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

import org.ambientlight.rfmbridge.Message;
import org.ambientlight.rfmbridge.WaitForResponseCondition;


/**
 * @author Florian Bornkessel
 * 
 */
public class WaitForShutterContactCondition extends WaitForResponseCondition {

	private Integer shutterAdress = 0;
	private Integer toAdress = 0;

	Message responseMessage;


	public WaitForShutterContactCondition(int shutterAdress, int toAdress) {
		this.shutterAdress = shutterAdress;
		this.toAdress = toAdress;
	}


	/*
	 * becomes true if an shutter contact became active and sent its state to the climatemanager. The shutter contact will be
	 * awake for 50msec. So the waiting request message will be directly send when this condition becomes true.
	 * 
	 * @see org.ambientlight.messages.Condition#fullfilled(java.lang.Object)
	 */
	@Override
	public boolean fullfilled(Message compare) {
		if (compare instanceof MaxShutterContactStateMessage
				&& ((MaxShutterContactStateMessage) compare).getFromAdress().equals(shutterAdress)
				&& ((MaxShutterContactStateMessage) compare).getToAdress().equals(toAdress))
			return true;
		return false;
	}
}
