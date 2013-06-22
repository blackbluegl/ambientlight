package org.ambientlight.scenery.rendering.programms;

import java.awt.image.BufferedImage;

import org.ambientlight.room.entities.LightObject;
import org.ambientlight.scenery.rendering.effects.RenderingEffect;

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
