package org.ambientlight.scenery.actor;

import java.io.Serializable;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.Value;
import org.codehaus.jackson.annotate.JsonTypeInfo;


@AlternativeValues(values = {
		@Value(displayName = "RGB Farbe", value = "org.ambientlight.scenery.actor.renderingprogram.SimpleColorRenderingProgramConfiguration"),
		@Value(displayName = "Tron", value = "org.ambientlight.scenery.actor.renderingprogram.TronRenderingProgrammConfiguration"),
		@Value(displayName = "Sonnenuntergang", value = "org.ambientlight.scenery.actor.renderingprogram.SunSetRenderingProgrammConfiguration") })
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class ActorConductConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;
}
