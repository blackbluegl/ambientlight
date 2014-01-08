package org.ambientlight.config.room.entities.lightobject.renderingprogram;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.Value;
import org.codehaus.jackson.annotate.JsonTypeInfo;


@AlternativeValues(values = {
		@Value(displayName = "RGB Farbe", value = "org.ambientlight.scenery.actor.renderingprogram.SimpleColorRenderingProgramConfiguration"),
		@Value(displayName = "Tron", value = "org.ambientlight.scenery.actor.renderingprogram.TronRenderingProgrammConfiguration"),
		@Value(displayName = "Sonnenuntergang", value = "org.ambientlight.scenery.actor.renderingprogram.SunSetRenderingProgrammConfiguration") })
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")

public abstract class RenderingProgramConfiguration {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
