package org.ambientlight.process.entities;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.process.events.EventManager;
import org.ambientlight.process.events.IEventListener;
import org.ambientlight.process.events.event.Event;
import org.ambientlight.process.trigger.EventTriggerConfiguration;


public class Process implements IEventListener {

	ProcessConfiguration config;
	EventManager eventManager;
	Map<Integer, Node> nodes = new HashMap<Integer, Node>();
	Token token;

	public void start() {
		// wait until event happens
		eventManager.register(this, config.eventTriggerConfiguration);
	}


	@Override
	public void handleEvent(Event event, EventTriggerConfiguration correlation) {
		token = new Token();
		token.nextNodeId = 0;

		token.valueType = TokenValueType.EVENT;
		token.data = event;

		Node currentNode = null;
		try {
			// Start action here until token nextNode is empty
			while (token.nextNodeId != null) {
				currentNode = nodes.get(token.nextNodeId);
				System.out.println("Process: " + config.id + " performes action by handler: "
						+ currentNode.handler.getClass().getSimpleName() + " in node: " + currentNode.config.id);
				currentNode.performAction(token);
			}
			System.out.println("Process: " + config.id + " finished.");
		} catch (Exception e) {
			System.out.println("Process: " + config.id + " stopped during an error in node: " + currentNode.config.id + ":");
			e.printStackTrace();
		}
	}
}