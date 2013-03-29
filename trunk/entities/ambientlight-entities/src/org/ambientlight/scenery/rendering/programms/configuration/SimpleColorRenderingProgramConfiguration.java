package org.ambientlight.scenery.rendering.programms.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("simpleColorRenderingProgram")
public class SimpleColorRenderingProgramConfiguration extends
		RenderingProgrammConfiguration {

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
