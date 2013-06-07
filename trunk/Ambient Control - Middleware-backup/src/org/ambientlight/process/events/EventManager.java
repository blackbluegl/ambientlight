package org.ambientlight.process.events;

import org.ambientlight.process.trigger.AlarmEventConfiguration;
import org.ambientlight.process.trigger.EventTriggerConfiguration;
import org.ambientlight.process.trigger.SceneryEntryEventConfiguration;
import org.ambientlight.scenery.actor.switching.SwitchingConfiguration;


public class EventManager {

	public void register(IEventListener eventListener, EventTriggerConfiguration triggerConfig) {

	}


	public void onSceneryChange(SceneryEntryEventConfiguration sceneryEntry) {

	}


	public void onAlarm(AlarmEventConfiguration alarm) {

	}


	public void onSwitchChange(SwitchingConfiguration config, boolean powerState) {

	}
}
