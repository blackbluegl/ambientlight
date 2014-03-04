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

package org.ambientlight.ws;

import java.io.IOException;

import org.ambientlight.room.entities.features.actor.types.SwitchType;
import org.ambientlight.room.entities.features.actor.types.SwitchableId;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;


/**
 * @author Florian Bornkessel
 * 
 */
public class SwitchableIdDeserializer extends JsonDeserializer<SwitchableId> {

	@Override
	public SwitchableId deserialize(JsonParser json, DeserializationContext context) throws IOException, JsonProcessingException {

		String tokens[] = json.getText().split("\\|");
		String id = tokens[0];
		SwitchType type = SwitchType.valueOf(tokens[1]);
		SwitchableId result = new SwitchableId(id, type);

		return result;
	}

}
