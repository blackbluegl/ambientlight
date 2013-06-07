package org.ambientlight.scenery.rendering.effects;

import java.awt.image.BufferedImage;

public interface RenderingEffect {

	public abstract void beforeRendering(BufferedImage background, BufferedImage pixelmap);

	/**
	 * returns a new instance of Image where the effect has been applied. If no
	 * changes will be applied the reference of the input will be returned.
	 * 
	 * @param input
	 * @return computed copy of the input
	 */
	public abstract BufferedImage afterRendering(BufferedImage input);
}