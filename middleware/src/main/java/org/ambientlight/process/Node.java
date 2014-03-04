package org.ambientlight.process;

import org.ambientlight.config.process.NodeConfiguration;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.process.handler.ActionHandlerException;


public class Node {

	public NodeConfiguration config;
	public AbstractActionHandler handler;


	public void performAction(Token token) throws ActionHandlerException {
		// if (handler != null) {
		handler.performAction(token);
		token.nextNodeId = handler.getNextNodeId();
		// } else {
		// token.nextNodeId = null;
		// }
	}
}
