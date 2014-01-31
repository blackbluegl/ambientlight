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

package org.ambientlight.room.entities.switches;

import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.room.entities.switches.SwitchManagerConfiguration;
import org.ambientlight.events.EventManager;
import org.ambientlight.events.SwitchEvent;
import org.ambientlight.room.Persistence;
import org.ambientlight.room.entities.FeatureFacade;
import org.ambientlight.room.entities.SwitchablesHandler;
import org.ambientlight.room.entities.features.actor.types.SwitchType;


/**
 * @author Florian Bornkessel
 * 
 */
public class SwitchManager implements SwitchablesHandler {

	private SwitchManagerConfiguration config;

	private CallBackManager callback;

	private EventManager eventManager;


	public SwitchManager(SwitchManagerConfiguration config, EventManager eventManager, CallBackManager callback,
			FeatureFacade entityFacade) {
		super();
		this.config = config;
		this.callback = callback;
		this.eventManager = eventManager;

		for (Switch currentSwitch : this.config.switches.values()) {
			entityFacade.registerSwitchable(this, currentSwitch, currentSwitch.type);
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.room.entities.SwitchablesHandler#setPowerState(java.
	 * lang.String, org.ambientlight.room.entities.switches.SwitchType, boolean)
	 */
	@Override
	public void setPowerState(String id, SwitchType type, boolean powerState) {
		if (config.switches.containsKey(id) == false) {
			System.out.println("SwitchManager handleSwitchChange(): got request from unknown device: =" + id);
			return;
		}

		Persistence.beginTransaction();

		Switch switchObject = config.switches.get(id);
		switchObject.setPowerState(powerState);

		Persistence.commitTransaction();

		eventManager.onEvent(new SwitchEvent(id, powerState, type.switchEventType));

		callback.roomConfigurationChanged();
	}
}
