package org.ambientlight.scenery.rendering.programms.configuration;

import org.ambientlight.scenery.SceneryConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("sunsetRenderingProgram")
public class SunSetRenderingProgrammConfiguration extends SceneryConfiguration {
	public int durationInMinutes=10;
}
