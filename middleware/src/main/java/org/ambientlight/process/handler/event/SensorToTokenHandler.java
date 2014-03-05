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

import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.config.process.handler.event.SensorToTokenConfiguration;
import org.ambientlight.process.Token;
import org.ambientlight.process.TokenSensorValue;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.process.handler.ActionHandlerException;
import org.ambientlight.process.handler.Util;
import org.ambientlight.room.entities.features.sensor.Sensor;


/**
 * @author Florian Bornkessel
 * 
 */
public class SensorToTokenHandler extends AbstractActionHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.handler.AbstractActionHandler#performAction(
	 * org.ambientlight.process.entities.Token)
	 */
	@Override
	public void performAction(Token token) throws ActionHandlerException {
		Util util = new Util(featureFacade);
		Sensor sensor = util.findSensor(this.getConfig().sensorId);
		TokenSensorValue value = new TokenSensorValue();
		value.sensorId = sensor.getSensorId();
		value.value = util.getDataFromSensor(sensor);
		token.valueType = DataTypeValidation.SENSOR;
		token.data = value;
	}


	private SensorToTokenConfiguration getConfig() {
		return (SensorToTokenConfiguration) this.config;
	}
}
