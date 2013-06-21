package org.ambientlight.process.handler;

import org.ambientlight.process.entities.Token;


public abstract class AbstractActionHandler {

	public AbstractActionHandlerConfiguration config;

	public int getNextNodeId(){
		return config.nextNodeId;
	}


	public abstract void performAction(Token token) throws ActionHandlerException;
}
