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

package org.ambientlight.config.room.entities.scenery;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Florian Bornkessel
 *
 */
public class SceneryManagerConfiguration {


	private static final long serialVersionUID = 1L;

	public AbstractSceneryConfiguration currentScenery;
	public Map<String, AbstractSceneryConfiguration> sceneries = new HashMap<String, AbstractSceneryConfiguration>();

}
