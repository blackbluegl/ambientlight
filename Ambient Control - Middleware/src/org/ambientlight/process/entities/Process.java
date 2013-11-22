package org.ambientlight.process.entities;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.config.process.EventProcessConfiguration;
import org.ambientlight.config.process.events.Event;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.process.eventmanager.EventManager;
import org.ambientlight.process.eventmanager.IEventListener;


public class Process implements IEventListener {

	public EventProcessConfiguration config;
	public EventManager eventManager;
	public Map<Integer, Node> nodes = new HashMap<Integer, Node>();
	Token token;


	public void start() {
		// wait until event happens
		for (Event event : config.eventTriggerConfigurations) {
			eventManager.register(this, event);
		}
	}


	public void suspend() {
		// wait until event happens
		for (Event event : config.eventTriggerConfigurations) {
			eventManager.unregister(this, event);
		}
	}


	@Override
	public void handleEvent(Event event) {
		token = new Token();
		token.nextNodeId = getFirstNode();

		token.valueType = DataTypeValidation.EVENT;
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


	private Integer getFirstNode() {
		for (Node currentNode : nodes.values()) {
			boolean foundPrevious = false;
			for (Node possiblePrevious : nodes.values()) {
				for (Integer nextNodeId : possiblePrevious.config.nextNodeIds) {
					if (nextNodeId.equals(currentNode.config.id)) {
						foundPrevious = true;
						break;
					}
					if (foundPrevious) {
						break;
					}
				}
			}
			if (foundPrevious == false)
				return currentNode.config.id;
		}
		return null;
	}
}