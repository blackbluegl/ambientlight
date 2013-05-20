package org.ambientlight.scenery;

import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class SceneryConfiguration {

	public boolean powerState = true;
	public boolean bypassOnSceneryChange = false;
}
