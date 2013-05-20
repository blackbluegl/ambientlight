package org.ambientlight.scenery.actor.rendering.programms.configuration;

import org.ambientlight.scenery.actor.ActorConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("sunsetRenderingProgram")
public class SunSetRenderingProgrammConfiguration extends ActorConfiguration {
	public int durationInMinutes=10;
}
