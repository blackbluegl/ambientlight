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
import org.ambientlight.events.BroadcastEvent;
import org.ambientlight.events.EventListener;
import org.ambientlight.events.EventManager;
import org.ambientlight.events.SwitchEvent;
import org.ambientlight.room.Persistence;


/**
 * @author Florian Bornkessel
 * 
 */
public class SwitchManager implements EventListener {

	private SwitchManagerConfiguration config;

	private CallBackManager callback;


	public SwitchManager(SwitchManagerConfiguration config, EventManager eventManager, CallBackManager callback) {
		super();
		this.config = config;
		this.callback = callback;

		// register listener for switchEvents
		for (Switch currentSwitch : config.switches.values()) {
			SwitchEvent svitchEventOn = new SwitchEvent(currentSwitch.getId(), true, currentSwitch.type.switchEventType);
			SwitchEvent svitchEventOff = new SwitchEvent(currentSwitch.getId(), false, currentSwitch.type.switchEventType);
			eventManager.register(this, svitchEventOn);
			eventManager.register(this, svitchEventOff);
		}
	}


	private void setSwitchState(String id, SwitchType type, boolean powerState) {
		if (config.switches.containsKey(id) == false) {
			System.out.println("SwitchManager handleSwitchChange(): got request from unknown device: =" + id);
			return;
		}

		Persistence.beginTransaction();

		Switch switchObject = config.switches.get(id);
		switchObject.setPowerState(powerState);

		Persistence.commitTransaction();


		callback.roomConfigurationChanged();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.events.EventListener#handleEvent(org.ambientlight.events
	 * .BroadcastEvent)
	 */
	@Override
	public void handleEvent(BroadcastEvent event) {
		SwitchEvent switchEvent = (SwitchEvent) event;
		setSwitchState(switchEvent.sourceId, SwitchType.forCode(switchEvent.type), switchEvent.powerState);
	}


}
