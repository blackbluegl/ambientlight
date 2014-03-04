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

package org.ambientlight.messages.rfm22bridge;

import org.ambientlight.config.messages.DispatcherType;
import org.ambientlight.messages.Message;


/**
 * @author Florian Bornkessel
 * 
 */
public class UnRegisterCorrelatorMessage extends Message {

	private DispatcherType dispatcherType;
	private String correlator;


	public UnRegisterCorrelatorMessage(DispatcherType dispatcherType, String correlator) {
		super();
		this.dispatcherType = dispatcherType;
		this.correlator = correlator;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.Message#getDispatcherType()
	 */
	@Override
	public DispatcherType getDispatcherType() {
		return dispatcherType;
	}


	public void setDispatcherType(DispatcherType dispatcherType) {
		this.dispatcherType = dispatcherType;
	}


	public String getCorrelator() {
		return correlator;
	}


	public void setCorrelator(String correlator) {
		this.correlator = correlator;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.Message#getCommand()
	 */
	@Override
	public String getCommand() {
		return "UNREGISTER_CORRELATION";
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.Message#getValue()
	 */
	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return getCorrelator();
	}
}
