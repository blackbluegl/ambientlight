package org.ambientlight.process.handler.actor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;


public class PowerstateHandlerConfiguration extends AbstractActionHandlerConfiguration{
	public Map<String,Boolean> powerStateConfiguration = new HashMap<String, Boolean>();

	public void addActorConfiguration(String actorName, boolean state){
		this.powerStateConfiguration.put(actorName, state);
	}

	public boolean getActorPowerState(String actorName){
		return this.powerStateConfiguration.get(actorName);
	}

	public Set<String> getActorNames(){
		return powerStateConfiguration.keySet();
	}
}
