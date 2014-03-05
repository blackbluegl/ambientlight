package org.ambientlight.process.handler;

import java.util.List;

import org.ambientlight.config.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.events.EventManager;
import org.ambientlight.process.Token;
import org.ambientlight.room.entities.FeatureFacade;


public abstract class AbstractActionHandler {

	public FeatureFacade featureFacade;
	public AbstractActionHandlerConfiguration config;
	public List<Integer> nodeIds;
	public EventManager eventManager;


	public Integer getNextNodeId() {
		if (nodeIds != null && nodeIds.size() > 0)
			return nodeIds.get(0);
		else
			return null;
	}


	public abstract void performAction(Token token) throws ActionHandlerException;
}
