package org.ambientlight.room.eventgenerator;

import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class EventGeneratorConfiguration {
	public String name;
}
