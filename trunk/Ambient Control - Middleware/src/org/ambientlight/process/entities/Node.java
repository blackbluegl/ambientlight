package org.ambientlight.process.entities;

import org.ambientlight.process.NodeConfiguration;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.process.handler.ActionHandlerException;


public class Node {

	public NodeConfiguration config;
	public AbstractActionHandler handler;


	public void performAction(Token token) throws ActionHandlerException {
		handler.performAction(token);
		token.nextNodeId=handler.getNextNodeId();
	}
}
