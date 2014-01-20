package org.ambientlight.room.entities.lightobject.effects;

import java.awt.image.BufferedImage;

import org.ambientlight.room.entities.lightobject.LightObject;
import org.ambientlight.room.entities.lightobject.effects.transitions.FadeInTransition;
import org.ambientlight.room.entities.lightobject.effects.transitions.FadeOutTransition;
import org.ambientlight.room.entities.lightobject.effects.transitions.SimpleFadeInTransitionImpl;
import org.ambientlight.room.entities.lightobject.effects.transitions.SimpleFadeOutTransitionImpl;
import org.ambientlight.room.entities.lightobject.util.ImageUtil;

public class RenderingEffectFactory {


	BufferedImage roomCanvas;


	public RenderingEffectFactory(BufferedImage roomCanvas) {
		this.roomCanvas = roomCanvas;
	}

	public FadeInTransition getFadeInEffect(LightObject lightObject) {

		BufferedImage background = ImageUtil.crop(roomCanvas, lightObject.configuration.xOffsetInRoom,
				lightObject.configuration.yOffsetInRoom, lightObject.configuration.width, lightObject.configuration.height);

		SimpleFadeInTransitionImpl fader = new SimpleFadeInTransitionImpl(background);
		return fader;
	}

	public FadeOutTransition getFadeOutEffect(LightObject lightObject) {
		BufferedImage background = lightObject.getPixelMapAfterEffect();

		SimpleFadeOutTransitionImpl fader = new SimpleFadeOutTransitionImpl(background);
		return fader;
	}

	public FadeInTransition getFadeEffect(LightObject lightObject) {

		BufferedImage background = lightObject.getPixelMapAfterEffect();

		SimpleFadeInTransitionImpl fader = new SimpleFadeInTransitionImpl(background);
		return fader;
	}
}
