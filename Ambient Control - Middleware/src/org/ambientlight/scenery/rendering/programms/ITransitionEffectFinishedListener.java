package org.ambientlight.scenery.rendering.programms;

import org.ambientlight.scenery.entities.LightObject;
import org.ambientlight.scenery.rendering.Renderer;

public interface ITransitionEffectFinishedListener {

	void lightObjectTransitionEffectFinished(Renderer renderer,
			LightObject lightObject);
}
