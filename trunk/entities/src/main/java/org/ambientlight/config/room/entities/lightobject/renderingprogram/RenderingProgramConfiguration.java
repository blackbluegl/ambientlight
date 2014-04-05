package org.ambientlight.config.room.entities.lightobject.renderingprogram;

import java.io.Serializable;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.Value;

import com.fasterxml.jackson.annotation.JsonTypeInfo;


@AlternativeValues(values = {
		@Value(displayName = "RGB Farbe", value = "org.ambientlight.config.room.entities.lightobject.renderingprogram.SimpleColorRenderingProgramConfiguration"),
		@Value(displayName = "Tron", value = "org.ambientlight.config.room.entities.lightobject.renderingprogram.TronRenderingProgrammConfiguration"),
		@Value(displayName = "Sonnenuntergang", value = "org.ambientlight.config.room.entities.lightobject.renderingprogram.SunSetRenderingProgrammConfiguration") })
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")

public abstract class RenderingProgramConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

}
