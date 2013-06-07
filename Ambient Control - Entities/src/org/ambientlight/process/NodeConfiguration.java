package org.ambientlight.process;

import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;


public class NodeConfiguration {
	public int id;
	
	/*Actionhandler that processes the node. If null is set the process will stop at this node*/
	public AbstractActionHandlerConfiguration actionHandler;
}
