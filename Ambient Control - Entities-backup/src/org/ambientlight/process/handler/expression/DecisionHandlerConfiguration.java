package org.ambientlight.process.handler.expression;

import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;


public class DecisionHandlerConfiguration extends AbstractActionHandlerConfiguration{
	ExpressionConfiguration expression;
	boolean invert;
	int nextAlternativeNodeId;
}
