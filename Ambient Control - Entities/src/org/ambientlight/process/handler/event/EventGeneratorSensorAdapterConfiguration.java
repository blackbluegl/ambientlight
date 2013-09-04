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

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.ValueBindingPath;
import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.process.handler.DataTypeValidation;
import org.ambientlight.process.validation.HandlerDataTypeValidation;


/**
 * @author Florian Bornkessel
 * 
 */
@HandlerDataTypeValidation(consumes = { DataTypeValidation.CONSUMES_NO_DATA }, generates = DataTypeValidation.EVENT)
public class EventGeneratorSensorAdapterConfiguration extends AbstractActionHandlerConfiguration {

	private static final long serialVersionUID = 1L;
	@AlternativeValues(valueBinding = { @ValueBindingPath(valueBinding = "eventGeneratorConfigurations.name") })
	@TypeDef(fieldType = FieldType.STRING)
	@Presentation(name = "Event auslesen und weitergeben", position = 0)
	public String eventSensorId;
}
