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

package org.ambientlight.process.handler.event;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.entities.Token;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.process.handler.ActionHandlerException;
import org.ambientlight.process.handler.DataTypeValidation;
import org.ambientlight.room.entities.EventSensor;


/**
 * @author Florian Bornkessel
 *
 */
public class EventGeneratorSensorAdapterHandler extends AbstractActionHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.handler.AbstractActionHandler#performAction(
	 * org.ambientlight.process.entities.Token)
	 */
	@Override
	public void performAction(Token token) throws ActionHandlerException {
		EventSensor sensor = AmbientControlMW.getRoom().getEventSensorById(this.getConfig().eventSensorId);
		token.data = sensor.getValue();
		token.valueType = DataTypeValidation.EVENT;
	}


	private EventGeneratorSensorAdapterConfiguration getConfig() {
		return (EventGeneratorSensorAdapterConfiguration) this.config;
	}
}
