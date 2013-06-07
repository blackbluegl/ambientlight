package org.ambientlight.process.handler;

import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;


public abstract class AbstractActionHandler {
	protected AbstractActionHandlerConfiguration config;
	
	public int getNextNodeId(){
		return config.nextNodeId;
	}

	public abstract void performAction(Object data);
}
