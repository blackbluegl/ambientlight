package org.ambientlight.process;

import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;


public class NodeConfiguration {

	public int id;

	/*
	 * Actionhandler that processes the node. If nextNodeId within is set to
	 * null the process will stop at this node
	 */
	public AbstractActionHandlerConfiguration actionHandler;
}
