package org.ambientlight.process.entities;

import org.ambientlight.process.NodeConfiguration;
import org.ambientlight.process.handler.AbstractActionHandler;


public class Node {
	NodeConfiguration config;
	AbstractActionHandler handler;

	public void performAction(Token token) {
		handler.performAction(token.data);
		token.nextNodeId=handler.getNextNodeId();
	}
}
