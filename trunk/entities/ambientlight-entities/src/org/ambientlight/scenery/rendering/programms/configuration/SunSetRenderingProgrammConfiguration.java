package org.ambientlight.scenery.rendering.programms.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("sunsetRenderingProgram")
public class SunSetRenderingProgrammConfiguration extends RenderingProgrammConfiguration {
	public int durationInMinutes=10;
}
