package org.ambientlight.process;

import java.io.Serializable;

import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;


public class NodeConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	public int id;

	/*
	 * Actionhandler that processes the node. If nextNodeId within is set to
	 * null the process will stop at this node
	 */
	public AbstractActionHandlerConfiguration actionHandler;
}
