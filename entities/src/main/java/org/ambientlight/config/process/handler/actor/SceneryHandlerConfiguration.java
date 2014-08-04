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

package org.ambientlight.config.process.handler.actor;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.annotations.valueprovider.SceneryNamesProvider;
import org.ambientlight.config.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.ws.process.validation.HandlerDataTypeValidation;


/**
 * @author Florian Bornkessel
 * 
 */
@HandlerDataTypeValidation(consumes = { DataTypeValidation.CONSUMES_NO_DATA, DataTypeValidation.SENSOR }, generates = DataTypeValidation.CREATES_NO_DATA)
public class SceneryHandlerConfiguration extends AbstractActionHandlerConfiguration {

	private static final long serialVersionUID = 1L;

	@Presentation(name = "Name Szenerie", position = 1)
	@TypeDef(fieldType = FieldType.BEAN_SELECTION)
	@AlternativeValues(values = { @Value(valueProvider = SceneryNamesProvider.class) })
	public String sceneryName;

	@Presentation(name = "Name aus Vorgängerknoten", position = 0, description = "Soll der Name der Szenerie nicht selbst festgelegt werden sondern dynamisch aus einem Vorgängerknoten im Prozess ermittelt werden?")
	@TypeDef(fieldType = FieldType.BOOLEAN)
	public boolean useTokenValue;
}
