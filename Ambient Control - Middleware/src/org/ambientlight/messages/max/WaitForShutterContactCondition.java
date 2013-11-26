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

package org.ambientlight.messages.max;

import org.ambientlight.messages.Message;
import org.ambientlight.messages.WaitForResponseCondition;


/**
 * @author Florian Bornkessel
 * 
 */
public class WaitForShutterContactCondition extends WaitForResponseCondition {

	private Integer shutterAdress = 0;

	Message responseMessage;


	public WaitForShutterContactCondition(int shutterAdress) {
		this.shutterAdress = shutterAdress;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.Condition#fullfilled(java.lang.Object)
	 */
	@Override
	public boolean fullfilled(Message compare) {
		if (compare instanceof MaxShutterContactStateMessage
				&& ((MaxShutterContactStateMessage) compare).getFromAdress().equals(shutterAdress))
			return true;
		return false;
	}
}
