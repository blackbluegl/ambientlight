package org.ambientlight.scenery.rendering.effects.transitions;

import java.awt.image.BufferedImage;

import org.ambientlight.scenery.rendering.util.ImageUtil;

public class SimpleFadeOutTransitionImpl implements FadeOutTransition {
	
	int BLENDING_STEPS = 50;

	int blendingStepsToGo = BLENDING_STEPS;
	
	BufferedImage background = null;
	BufferedImage foreground = null;

	public SimpleFadeOutTransitionImpl(BufferedImage pixelmap){
		this.foreground= ImageUtil.deepCopy(pixelmap);
	}
	
	@Override
	public BufferedImage afterRendering(BufferedImage input) {
		if (this.blendingStepsToGo !=0) {
			float weight = ((float)blendingStepsToGo / (float)BLENDING_STEPS);
			this.blendingStepsToGo--;
			return ImageUtil.blend(foreground, background, weight);
		} else {
			return input;
		}
	}

	@Override
	public boolean isFinished() {
		return this.blendingStepsToGo==0? true: false;
	}

	@Override
	public void beforeRendering(BufferedImage background, BufferedImage pixelmap) {
		this.background=background;
	}
	


}
