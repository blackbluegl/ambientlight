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

package org.ambientlight.process.events;

import org.ambientlight.process.trigger.AlarmEventConfiguration;
import org.ambientlight.process.trigger.SceneryEntryEventConfiguration;
import org.ambientlight.scenery.actor.switching.SwitchingConfiguration;


/**
 * @author Florian Bornkessel
 *
 */
public interface IEventManagerClient {

	public abstract void onSceneryChange(SceneryEntryEventConfiguration sceneryEntry);


	public abstract void onAlarm(AlarmEventConfiguration alarm);


	public abstract void onSwitchChange(SwitchingConfiguration config, boolean powerState);

}