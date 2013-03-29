package org.ambientlight.scenery.rendering.programms.configuration;

import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class RenderingProgrammConfiguration {
	public String sceneryName;
	public boolean powerState = true;
}
