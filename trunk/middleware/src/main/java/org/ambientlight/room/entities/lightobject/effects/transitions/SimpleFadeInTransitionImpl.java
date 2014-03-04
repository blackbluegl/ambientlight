package org.ambientlight.room.entities.lightobject.effects.transitions;

import java.awt.image.BufferedImage;

import org.ambientlight.room.entities.lightobject.util.ImageUtil;


public class SimpleFadeInTransitionImpl implements FadeInTransition {

	int BLENDING_STEPS = 50;

	int blendingStepsToGo = BLENDING_STEPS;

	BufferedImage sourceImage;


	public SimpleFadeInTransitionImpl(BufferedImage input) {

		this.sourceImage = ImageUtil.deepCopy(input);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.scenery.rendering.effects.RenderingEffects#afterRendering
	 * (java.awt.image.BufferedImage)
	 */
	@Override
	public BufferedImage afterRendering(BufferedImage input) {
		if (this.blendingStepsToGo != 0) {
			float weight = ((float) blendingStepsToGo / (float) BLENDING_STEPS);
			this.blendingStepsToGo--;
			return ImageUtil.blend(this.sourceImage, input, weight);
		} else
			return input;
	}


	@Override
	public boolean isFinished() {
		return this.blendingStepsToGo == 0 ? true : false;
	}


	@Override
	public void beforeRendering(BufferedImage background, BufferedImage pixelmap) {
		// TODO Auto-generated method stub

	}
}
