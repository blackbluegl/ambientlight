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

package org.ambientlight.process;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.codehaus.jackson.annotate.JsonTypeInfo;


/**
 * @author Florian Bornkessel
 *
 */
@AlternativeValues(values = { @Value(displayName = "Eventgesteuerter Prozess", value = "org.ambientlight.process.EventProcessConfiguration") })
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class ProcessConfiguration {

	@Presentation(name = "Name des Prozesses", position = 0)
	@TypeDef(fieldType = FieldType.STRING)
	public String id;

	public Map<Integer, NodeConfiguration> nodes = new HashMap<Integer, NodeConfiguration>();
}