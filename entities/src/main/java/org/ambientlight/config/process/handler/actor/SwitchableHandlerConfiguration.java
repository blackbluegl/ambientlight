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

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.config.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.ws.process.validation.HandlerDataTypeValidation;


/**
 * @author Florian Bornkessel
 * 
 */
@HandlerDataTypeValidation(consumes = { DataTypeValidation.CONSUMES_NO_DATA, DataTypeValidation.BOOLEAN }, generates = DataTypeValidation.CREATES_NO_DATA)
public class SwitchableHandlerConfiguration extends AbstractActionHandlerConfiguration {

	private static final long serialVersionUID = 1L;

	@Presentation(name = "Zustand aus Vorgängerknoten", position = 0, description = "Soll der Zustand der Schalter nicht selbst festgelegt werden sondern dynamisch aus einem Vorgängerknoten im Prozess ermittelt werden?")
	@TypeDef(fieldType = FieldType.BOOLEAN)
	public boolean useTokenValue;

	@Presentation(name = "Schalter einschalten", position = 1)
	@TypeDef(fieldType = FieldType.BOOLEAN)
	public boolean powerState;

	@Presentation(name = "Zustand invertieren", position = 1, description = "Sinnvoll wenn der Zustand dynamisch ermittelt wird.")
	@TypeDef(fieldType = FieldType.BOOLEAN)
	public boolean invert;

	@Presentation(name = "Schalter auswählen", position = 3)
	@TypeDef(fieldType = FieldType.SELECTION_LIST)
	@AlternativeValues(values = { @Value(valueProvider = "org.ambientlight.annotations.valueprovider.SwitchesIdsProvider") })
	public List<EntityId> switcheables = new ArrayList<EntityId>();

	@Presentation(name = "Event feuern", position = 4, description = "Schalter können mitteilen das sie geschaltet wurden. Andere Entitäten können auf dieses Ereignis reagieren. Achtung: ungeschickt angewandt sind Deadlocks möglich!")
	@TypeDef(fieldType = FieldType.BOOLEAN)
	public boolean fireEvent;
}
