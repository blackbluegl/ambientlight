package org.ambientlight.process.handler;

import java.util.List;

import org.ambientlight.process.entities.Token;


public abstract class AbstractActionHandler {

	public AbstractActionHandlerConfiguration config;
	public List<Integer> nodeIds;

	public Integer getNextNodeId() {
		return nodeIds.get(0);
	}


	public abstract void performAction(Token token) throws ActionHandlerException;
}
