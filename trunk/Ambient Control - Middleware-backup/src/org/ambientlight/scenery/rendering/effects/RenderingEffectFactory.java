package org.ambientlight.scenery.rendering.effects;

import java.awt.image.BufferedImage;

import org.ambientlight.scenery.entities.LightObject;
import org.ambientlight.scenery.entities.Room;
import org.ambientlight.scenery.rendering.effects.transitions.FadeInTransition;
import org.ambientlight.scenery.rendering.effects.transitions.FadeOutTransition;
import org.ambientlight.scenery.rendering.effects.transitions.SimpleFadeInTransitionImpl;
import org.ambientlight.scenery.rendering.effects.transitions.SimpleFadeOutTransitionImpl;
import org.ambientlight.scenery.rendering.util.ImageUtil;

public class RenderingEffectFactory {
	Room room;

	public RenderingEffectFactory(Room room) {
		this.room = room;
	}

	public FadeInTransition getFadeInEffect(LightObject lightObject) {

		BufferedImage background = ImageUtil.crop(room.getRoomBitMap(), lightObject.getConfiguration().xOffsetInRoom, 
				lightObject.getConfiguration().yOffsetInRoom, lightObject.getConfiguration().width, 
				lightObject.getConfiguration().height);
		
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
