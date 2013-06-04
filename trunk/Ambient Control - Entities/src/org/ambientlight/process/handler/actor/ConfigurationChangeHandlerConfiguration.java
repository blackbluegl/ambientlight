package org.ambientlight.process.handler.actor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.scenery.actor.ActorConductConfiguration;


public class ConfigurationChangeHandlerConfiguration extends AbstractActionHandlerConfiguration{
	private Map<String, ActorConductConfiguration> actorConfiguration = new HashMap<String, ActorConductConfiguration>();
	
	public void addActorConfiguration(String actorName, ActorConductConfiguration configuration){
		this.actorConfiguration.put(actorName, configuration);
	}
	
	public ActorConductConfiguration getActorConfiguration(String actorName){
		return this.actorConfiguration.get(actorName);
	}
	
	public Set<String> getActorNames(){
		return actorConfiguration.keySet();
	}
}
