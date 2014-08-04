package org.ambientlight.config.room.entities.lightobject.renderingprogram;

import java.io.Serializable;

import org.ambientlight.annotations.AlternativeClassValues;
import org.ambientlight.annotations.ClassValue;

import com.fasterxml.jackson.annotation.JsonTypeInfo;


@AlternativeClassValues(values = {
		@ClassValue(displayValue = "Farbe auswählen", newClassInstanceType = SimpleColorRenderingProgramConfiguration.class),
		@ClassValue(displayValue = "Tron auswählen", newClassInstanceType = TronRenderingProgrammConfiguration.class),
		@ClassValue(displayValue = "Sonnenuntergang auswählen", newClassInstanceType = SunSetRenderingProgrammConfiguration.class) })
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class RenderingProgramConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

}
