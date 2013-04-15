package org.ambientlight.scenery.rendering.programms.configuration;

import org.ambientlight.scenery.SceneryConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("TronRenderingProgram")
public class TronRenderingProgrammConfiguration extends SceneryConfiguration {

	int speed;
	int lightPointAmount;
	
	public int getSpeed() {
		return speed;
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public int getLightPointAmount() {
		return lightPointAmount;
	}
	
	public void setLightPointAmount(int lightPointAmount) {
		this.lightPointAmount = lightPointAmount;
	}
}
