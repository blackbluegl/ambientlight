package org.ambientlight.config.process.handler;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;



@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class AbstractActionHandlerConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;
}
