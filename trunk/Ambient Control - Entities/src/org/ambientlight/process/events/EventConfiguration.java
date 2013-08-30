package org.ambientlight.process.events;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class EventConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	public String eventGeneratorName;
}
