package org.ambientlight.config.room.entities.lightobject.renderingprogram;

import java.io.Serializable;

import org.ambientlight.annotations.AlternativeClassValues;
import org.ambientlight.annotations.ClassValue;

import com.fasterxml.jackson.annotation.JsonTypeInfo;


@AlternativeClassValues(values = {
		@ClassValue(displayValue = "RGB Farbe", value = "org.ambientlight.config.room.entities.lightobject.renderingprogram.SimpleColorRenderingProgramConfiguration"),
		@ClassValue(displayValue = "Tron", value = "org.ambientlight.config.room.entities.lightobject.renderingprogram.TronRenderingProgrammConfiguration"),
		@ClassValue(displayValue = "Sonnenuntergang", value = "org.ambientlight.config.room.entities.lightobject.renderingprogram.SunSetRenderingProgrammConfiguration") })
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class RenderingProgramConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

}
