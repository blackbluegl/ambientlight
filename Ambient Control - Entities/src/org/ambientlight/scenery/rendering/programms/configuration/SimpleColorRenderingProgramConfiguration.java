package org.ambientlight.scenery.rendering.programms.configuration;

import org.ambientlight.scenery.SceneryConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("simpleColorRenderingProgram")
public class SimpleColorRenderingProgramConfiguration extends
		SceneryConfiguration {

	int r;
	int g;
	int b;

	
	public int getR() {
		return r;
	}

	public void setR(int r) {
		this.r = r;
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}

}
