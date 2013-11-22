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

package test;

import java.util.Date;

import org.ambientlight.messages.Message;
import org.ambientlight.messages.MessageListener;
import org.ambientlight.messages.QeueManager.State;


/**
 * @author Florian Bornkessel
 * 
 */
public class MessageDump implements MessageListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.messages.MessageListener#handleMessage(org.ambientlight
	 * .messages.Message)
	 */
	@Override
	public void handleMessage(Message message) {

		System.out.println((new Date(System.currentTimeMillis()).toString() + " - new Message:\n" + message.toString()) + "\n");
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.messages.MessageListener#handleResponseMessages(org.
	 * ambientlight.messages.QeueManager.State,
	 * org.ambientlight.messages.Message, org.ambientlight.messages.Message)
	 */
	@Override
	public void handleResponseMessages(State state, Message response, Message request) {
		// TODO Auto-generated method stub

	}
}
