package org.ambientlight.rendering.programms;

import java.awt.image.BufferedImage;

import org.ambientlight.rendering.effects.RenderingEffect;
import org.ambientlight.room.entities.LightObject;

public abstract class RenderingProgramm{

	RenderingEffect effect;
	
	public synchronized void addEffect(RenderingEffect effect) {
		this.effect=effect;
	}

	public synchronized void removeEffect() {
		this.effect = null;
	}

	public abstract BufferedImage renderLightObject(LightObject lightObject);

	public RenderingEffect getEffect() {
		return this.effect;
	}
	
	public abstract boolean hasDirtyRegion();
}
