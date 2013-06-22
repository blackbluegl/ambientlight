package org.ambientlight.process.handler;

import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class AbstractActionHandlerConfiguration {

	public Integer nextNodeId;
}
