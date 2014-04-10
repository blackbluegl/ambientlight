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

import org.ambientlight.room.entities.features.EntityId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;


/**
 * @author Florian Bornkessel
 * 
 */
public class EntityIdDeserializer extends KeyDeserializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fasterxml.jackson.databind.KeyDeserializer#deserializeKey(java.lang .String,
	 * com.fasterxml.jackson.databind.DeserializationContext)
	 */
	@Override
	public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		return EntityId.fromString(key);
	}

}
