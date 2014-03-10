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

package org.ambientlight;

import javax.ws.rs.ext.ContextResolver;

import org.ambientlight.ws.SwitchableIdModule;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Florian Bornkessel
 * 
 */
public class RestConfig extends ResourceConfig {

	public RestConfig() {
		packages("org.ambientlight.webservice");

		register(new ContextResolver<ObjectMapper>() {

			@Override
			public ObjectMapper getContext(Class<?> type) {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.registerModule(new SwitchableIdModule());
				return objectMapper;
			}
		});
	}
}
