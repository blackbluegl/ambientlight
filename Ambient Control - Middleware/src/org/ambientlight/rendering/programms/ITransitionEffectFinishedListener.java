package org.ambientlight.rendering.programms;

import org.ambientlight.rendering.Renderer;
import org.ambientlight.room.entities.LightObject;

public interface ITransitionEffectFinishedListener {

	void lightObjectTransitionEffectFinished(Renderer renderer,
			LightObject lightObject);
}
