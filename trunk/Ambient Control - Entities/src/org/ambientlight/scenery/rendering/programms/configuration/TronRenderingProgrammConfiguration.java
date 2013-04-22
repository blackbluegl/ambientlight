package org.ambientlight.scenery.rendering.programms.configuration;

import org.ambientlight.scenery.SceneryConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("TronRenderingProgram")
public class TronRenderingProgrammConfiguration extends SceneryConfiguration implements Cloneable{

	int r;
	int g;
	int b;
	double lightImpact;
	double tailLength;
	double sparkleStrength;
	double sparkleSize;
	int lightPointAmount;
	double speed;


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


	public double getLightImpact() {
		return lightImpact;
	}


	public void setLightImpact(double lightImpact) {
		this.lightImpact = lightImpact;
	}


	public double getTailLength() {
		return tailLength;
	}


	public void setTailLength(double tailLength) {
		this.tailLength = tailLength;
	}


	public double getSparkleStrength() {
		return sparkleStrength;
	}


	public void setSparkleStrength(double sparkleStrength) {
		this.sparkleStrength = sparkleStrength;
	}


	public double getSparkleSize() {
		return sparkleSize;
	}


	public void setSparkleSize(double sparkleSize) {
		this.sparkleSize = sparkleSize;
	}


	public int getLightPointAmount() {
		return lightPointAmount;
	}


	public void setLightPointAmount(int lightPointAmount) {
		this.lightPointAmount = lightPointAmount;
	}


	public double getSpeed() {
		return speed;
	}


	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public TronRenderingProgrammConfiguration clone(){
		try {
			return (TronRenderingProgrammConfiguration) super.clone();
		} catch (CloneNotSupportedException e) {
			//this should not happen
			e.printStackTrace();
		}
		return null;
	}
}
