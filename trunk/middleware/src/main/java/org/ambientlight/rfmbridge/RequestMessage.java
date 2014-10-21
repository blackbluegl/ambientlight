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

package org.ambientlight.rfmbridge;

/**
 * @author Florian Bornkessel
 * 
 */
public interface RequestMessage {

	public int getTimeOutSec();


	public int getRetryCount();


	/**
	 * define correlator value for response messages. If the correlation value of the response messages is equal to this message
	 * correlator, the queueManager calls onResponse() from the sender of the request message.
	 * 
	 * @return correlation value for response messages
	 */
	public String getCorrelation();
}
