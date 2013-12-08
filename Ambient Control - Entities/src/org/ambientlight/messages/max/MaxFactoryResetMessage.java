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

import org.ambientlight.messages.AckRequestMessage;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxFactoryResetMessage extends MaxMessage implements AckRequestMessage {

	public MaxFactoryResetMessage() {
		payload = new byte[10];
		setMessageType(MaxMessageType.RESET);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.AckRequestMessage#getTimeOutSec()
	 */
	@Override
	public int getTimeOutSec() {
		return 5;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.AckRequestMessage#getRetryCount()
	 */
	@Override
	public int getRetryCount() {
		return 5;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.AckRequestMessage#getCorrelation()
	 */
	@Override
	public String getCorrelation() {
		return String.valueOf(getSequenceNumber());
	}
}
