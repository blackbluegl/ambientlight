package org.ambientlight.scenery.actor;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonTypeInfo;



@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class ActorConductConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;
}
