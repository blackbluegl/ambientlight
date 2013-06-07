package org.ambientlight.process.entities;

import java.util.Map;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.process.events.IEventListener;
import org.ambientlight.process.trigger.EventTriggerConfiguration;


public class Process implements IEventListener{
	ProcessConfiguration config;
	Map<Integer,Node> nodes;
	Token token;

	public void start(){
		//wait until event happens
		AmbientControlMW.getEventManager().register(this, config.eventTriggerConfiguration);
	}

	@Override
	public void handleEvent(EventTriggerConfiguration event, String eventGeneratorName) {
		//Start action here until token nextNode is empty
		while(token.nextNodeId!=null){
			Node currentNode = nodes.get(token.nextNodeId); 
			currentNode.performAction(token);
		}
	}
}
