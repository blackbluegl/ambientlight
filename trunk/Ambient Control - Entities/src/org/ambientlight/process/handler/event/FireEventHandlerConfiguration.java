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
import org.ambientlight.annotations.Value;
import org.ambientlight.process.events.EventConfiguration;
import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;


/**
 * @author Florian Bornkessel
 *
 */
public class FireEventHandlerConfiguration extends AbstractActionHandlerConfiguration {

	@Presentation(name = "Events aus Prozess verwenden")
	@TypeDef(fieldType = FieldType.BOOLEAN)
	public boolean useFromToken;

	@TypeDef(fieldType = FieldType.BEAN)
	@AlternativeValues(values = {
			@Value(displayName = "Scenario wescheln", value = "org.ambientlight.process.events.SceneryEntryEventConfiguration"),
			@Value(displayName = "Schalter umlegen", value = "org.ambientlight.process.events.SwitchEventConfiguration") })
	public EventConfiguration event;
}
