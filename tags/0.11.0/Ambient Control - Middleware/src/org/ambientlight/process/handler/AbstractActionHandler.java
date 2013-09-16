package org.ambientlight.process.handler;

import java.util.List;

import org.ambientlight.process.entities.Token;


public abstract class AbstractActionHandler {

	public AbstractActionHandlerConfiguration config;
	public List<Integer> nodeIds;


	public Integer getNextNodeId() {
		if (nodeIds != null && nodeIds.size() > 0)
			return nodeIds.get(0);
		else
			return null;
	}


	public abstract void performAction(Token token) throws ActionHandlerException;
}
