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

package org.ambient.control.processes.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.ClassDescription;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Group;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.room.entities.sceneries.Scenery;


/**
 * @author Florian Bornkessel
 * 
 */
@ClassDescription(groups = { @Group(name = "ALLGEMEIN", position = 1, description = "Szenarien können zur Steuerung von Prozessen verwendet werden. Wichtig: wird ein Szenario in einem Prozess verwendet und gelöscht, so wird der Prozess nicht mehr ausgeführt.") })
public class SceneriesWrapper implements Serializable {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.SIMPLE_LIST)
	@Presentation(name = "Szenarien verwalten", position = 0, groupPosition = 1)
	@AlternativeValues(values = { @Value(displayName = "Anwenderszenario", value = "org.ambientlight.scenery.UserSceneryConfiguration") })
	public List<Scenery> sceneries = new ArrayList<Scenery>();
}
