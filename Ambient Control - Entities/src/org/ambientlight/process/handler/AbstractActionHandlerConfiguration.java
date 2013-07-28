package org.ambientlight.process.handler;

import java.io.Serializable;

import org.ambientlight.annotations.Alternative;
import org.ambientlight.annotations.Alternatives;
import org.codehaus.jackson.annotate.JsonTypeInfo;


@Alternatives(alternatives = {
		@Alternative(name = "Konfiguration ändern", className = "org.ambientlight.process.handler.actor.ConfigurationChangeHandlerConfiguration"),
		@Alternative(name = "Powerstate ändern", className = "org.ambientlight.process.handler.actor.PowerstateHandlerConfiguration") })
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class AbstractActionHandlerConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;
	public Integer nextNodeId;
}
