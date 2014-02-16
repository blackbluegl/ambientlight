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

import java.io.Serializable;
import java.util.List;

import org.ambientlight.config.messages.QeueManagerConfiguration;
import org.ambientlight.config.process.ProcessManagerConfiguration;
import org.ambientlight.config.room.entities.alarm.AlarmManagerConfiguration;
import org.ambientlight.config.room.entities.climate.ClimateManagerConfiguration;
import org.ambientlight.config.room.entities.lightobject.LightObjectManagerConfiguration;
import org.ambientlight.config.room.entities.remoteswitches.RemoteSwitchManagerConfiguration;
import org.ambientlight.config.room.entities.scenery.SceneryManagerConfiguration;
import org.ambientlight.config.room.entities.switches.SwitchManagerConfiguration;
import org.ambientlight.room.entities.features.actor.Switchable;


/**
 * @author Florian Bornkessel
 * 
 */
public class Room implements Serializable {

	private static final long serialVersionUID = 1L;

	public String roomName;

	public QeueManagerConfiguration qeueManager;
	public ClimateManagerConfiguration climateManager;
	public SwitchManagerConfiguration switchesManager;
	public RemoteSwitchManagerConfiguration remoteSwitchesManager;
	public AlarmManagerConfiguration alarmManager;
	public SceneryManagerConfiguration sceneriesManager;
	public ProcessManagerConfiguration processManager;
	public LightObjectManagerConfiguration lightObjectManager;

	public List<Switchable> switchables;
}
