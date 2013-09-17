package org.ambientlight.rendering.effects;

import java.awt.image.BufferedImage;

import org.ambientlight.rendering.effects.transitions.FadeInTransition;
import org.ambientlight.rendering.effects.transitions.FadeOutTransition;
import org.ambientlight.rendering.effects.transitions.SimpleFadeInTransitionImpl;
import org.ambientlight.rendering.effects.transitions.SimpleFadeOutTransitionImpl;
import org.ambientlight.rendering.util.ImageUtil;
import org.ambientlight.room.Room;
import org.ambientlight.room.entities.LightObject;

public class RenderingEffectFactory {
	Room room;

	public RenderingEffectFactory(Room room) {
		this.room = room;
	}

	public FadeInTransition getFadeInEffect(LightObject lightObject) {

		BufferedImage background = ImageUtil.crop(room.getRoomBitMap(), lightObject.configuration.xOffsetInRoom,
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
