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

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.entities.Token;
import org.ambientlight.process.entities.TokenValueType;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.process.handler.ActionHandlerException;
import org.ambientlight.room.entities.Sensor;


/**
 * @author Florian Bornkessel
 * 
 */
public class DecissionActionHandler extends AbstractActionHandler {

	boolean takeDefaultTransition = true;


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

		for (String dataproviderName : getConfig().expressionConfiguration.sensorNames) {
			Sensor dataprovider = AmbientControlMW.getRoom().sensors.get(dataproviderName);
			evaluator.putVariable(dataproviderName, dataprovider.getValue().toString());
		}
		try {
			takeDefaultTransition = evaluator.getBooleanResult(this.getConfig().expressionConfiguration.expression);
			token.data = takeDefaultTransition;
			token.valueType = TokenValueType.BOOLEAN;
		} catch (EvaluationException e) {
			ActionHandlerException ae = new ActionHandlerException(e);
			throw ae;
		}
	}


	DecisionHandlerConfiguration getConfig() {
		return (DecisionHandlerConfiguration) this.config;
	}


	@Override
	public Integer getNextNodeId() {
		Integer nextNodeId = takeDefaultTransition ? config.nextNodeId : getConfig().nextAlternativeNodeId;
		System.out.println("DecissionHandler: takes transition to node: " + nextNodeId);
		return nextNodeId;
	}

}
