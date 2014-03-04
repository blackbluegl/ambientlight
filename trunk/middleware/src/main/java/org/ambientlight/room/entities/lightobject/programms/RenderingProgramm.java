package org.ambientlight.room.entities.lightobject.programms;

import java.awt.image.BufferedImage;

import org.ambientlight.room.entities.lightobject.RenderObject;
import org.ambientlight.room.entities.lightobject.effects.RenderingEffect;

public abstract class RenderingProgramm{

	RenderingEffect effect;


	public void addEffect(RenderingEffect effect) {
		this.effect=effect;
	}


	public void removeEffect() {
		this.effect = null;
	}

	public abstract BufferedImage renderLightObject(RenderObject lightObject);

	public RenderingEffect getEffect() {
		return this.effect;
	}

	public abstract boolean hasDirtyRegion();
}
