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

package org.ambientlight.config.process.handler.event;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.config.process.events.Event;
import org.ambientlight.config.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.config.process.validation.HandlerDataTypeValidation;


/**
 * @author Florian Bornkessel
 *
 */
@HandlerDataTypeValidation(consumes = { DataTypeValidation.EVENT, DataTypeValidation.CONSUMES_NO_DATA }, generates = DataTypeValidation.CREATES_NO_DATA)
public class FireEventHandlerConfiguration extends AbstractActionHandlerConfiguration {

	private static final long serialVersionUID = 1L;

	@Presentation(name = "Events aus Prozess verwenden", position = 0)
	@TypeDef(fieldType = FieldType.BOOLEAN)
	public boolean useFromToken;

	@TypeDef(fieldType = FieldType.BEAN)
	@AlternativeValues(values = {
			@Value(displayName = "Scenario wescheln", value = "org.ambientlight.process.events.SceneryEntryEventConfiguration"),
			@Value(displayName = "Schalter umlegen", value = "org.ambientlight.process.events.SwitchEventConfiguration") })
	public Event event;
}
