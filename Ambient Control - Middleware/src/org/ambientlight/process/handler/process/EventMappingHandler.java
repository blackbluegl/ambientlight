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

package org.ambientlight.process.handler.process;

import org.ambientlight.process.entities.Token;
import org.ambientlight.process.entities.TokenValueType;
import org.ambientlight.process.events.event.SwitchEvent;
import org.ambientlight.process.handler.AbstractActionHandler;


/**
 * @author Florian Bornkessel
 *
 */
public class EventMappingHandler extends AbstractActionHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.handler.AbstractActionHandler#performAction(
	 * org.ambientlight.process.entities.TokenValueType, java.lang.Object)
	 */
	@Override
	public void performAction(Token token) {
		if (token.valueType.equals(TokenValueType.EVENT)) {
			if (token.data instanceof SwitchEvent) {
				SwitchEvent event = (SwitchEvent) token.data;
				token.data = event.powerState;
				token.valueType = TokenValueType.BOOLEAN;
			}
		}
	}

}
