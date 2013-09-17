package org.ambientlight.scenery.actor.renderingprogram;

import org.ambientlight.scenery.actor.ActorConductConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("sunsetRenderingProgram")
public class SunSetRenderingProgrammConfiguration extends RenderingProgramConfiguration {
	public int durationInMinutes=10;
}
