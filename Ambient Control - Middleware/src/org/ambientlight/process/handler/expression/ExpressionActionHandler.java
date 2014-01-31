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

package org.ambientlight.process.handler.expression;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.config.process.handler.expression.ExpressionHandlerConfiguration;
import org.ambientlight.process.Token;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.process.handler.ActionHandlerException;
import org.ambientlight.room.entities.features.sensor.ScenerySensor;
import org.ambientlight.room.entities.features.sensor.Sensor;
import org.ambientlight.room.entities.features.sensor.TemperatureSensor;
import org.ambientlight.room.entities.features.sensor.types.TemperatureSensorId;
import org.ambientlight.room.entities.features.sensor.types.TemperatureSensorType;


/**
 * @author Florian Bornkessel
 * 
 */
public class ExpressionActionHandler extends AbstractActionHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.process.handler.AbstractActionHandler#performAction(
	 * org.ambientlight.process.entities.Token)
	 */
	@Override
	public void performAction(Token token) throws ActionHandlerException {
		Evaluator evaluator = new Evaluator();

		evaluator.putVariable("tokenValue", token.data.toString());

		for (String dataproviderName : this.extractDataProvider(getConfig().expressionConfiguration.expression)) {
			Sensor sensor = findSensor(dataproviderName);
			evaluator.putVariable(dataproviderName, getValueFromDataProvider(sensor));
		}
		try {
			String resultString = evaluator.evaluate(this.getConfig().expressionConfiguration.expression);
			token.data = Double.parseDouble(resultString);
			token.valueType = DataTypeValidation.NUMERIC;
		} catch (EvaluationException e) {
			ActionHandlerException ae = new ActionHandlerException(e);
			throw ae;
		}
	}


	/**
	 * @param dataproviderName
	 * @return
	 */
	protected Sensor findSensor(String dataproviderName) {
		String[] strinkTokens = dataproviderName.split(":");
		SensorCategory category = SensorCategory.valueOf(strinkTokens[0]);

		switch (category) {
		case SCENERY:
			return AmbientControlMW.getRoom().featureFacade.getScenerySensor();
		case TEMPERATURE:
			TemperatureSensorType sensorType = TemperatureSensorType.valueOf(strinkTokens[1]);
			TemperatureSensorId id = new TemperatureSensorId();
			id.type = sensorType;
			id.id = strinkTokens[2];
			return AmbientControlMW.getRoom().featureFacade.getTemperatureSensors().get(id);
		default:
			break;
		}
		return null;
	}


	protected String getValueFromDataProvider(Sensor sensor) {
		if (sensor instanceof TemperatureSensor)
			return new DecimalFormat("#.##").format(((TemperatureSensor) sensor).getTemperature());
		else if (sensor instanceof ScenerySensor)
			return ((ScenerySensor) sensor).getCurrentScenery().id;
		else
			return "0.0";
	}


	protected List<String> extractDataProvider(String expression) {
		String[] tokens = expression.split("#\\{");

		List<String> result = new ArrayList<String>();
		for (String currentToken : tokens) {
			if (currentToken.isEmpty() == false) {
				result.add(currentToken.substring(0, currentToken.indexOf('}')));
			}
		}
		return result;
	}


	ExpressionHandlerConfiguration getConfig() {
		return (ExpressionHandlerConfiguration) this.config;
	}
}
