package org.ambientlight.process;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.process.EventProcessConfiguration;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.events.BroadcastEvent;
import org.ambientlight.events.EventListener;
import org.ambientlight.events.EventManager;


public class Process implements EventListener {

	public CallBackManager callback;
	public EventProcessConfiguration config;
	public EventManager eventManager;
	public Map<Integer, Node> nodes = new HashMap<Integer, Node>();
	Token token;


	public void start() {
		// wait until event happens
		for (BroadcastEvent event : config.eventTriggerConfigurations) {
			eventManager.register(this, event);
		}
	}


	public void suspend() {
		// wait until event happens
		for (BroadcastEvent event : config.eventTriggerConfigurations) {
			eventManager.unregister(this, event);
		}
	}


	@Override
	public void handleEvent(BroadcastEvent event) {
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

			callback.roomConfigurationChanged();

			System.out.println("Process: " + config.id + " finished successfully.");
		} catch (Exception e) {
			System.out.println("Process: " + config.id + " canceled with an error in node: " + currentNode.config.id + ":");
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