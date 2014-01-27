package org.ambientlight.process.handler.actor;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.process.handler.actor.RenderingProgrammChangeHandlerConfiguration;
import org.ambientlight.process.Token;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.room.entities.features.actor.Renderable;


public class RenderingProgrammChangeHandler extends AbstractActionHandler {

	@Override
	public void performAction(Token data) {
		for (String currentActorName : getConfig().renderConfig.keySet()) {
			Renderable renderable = getConfig().renderConfig.get(currentActorName);

			AmbientControlMW.getRoom().lightObjectManager.setRenderingConfiguration(
					renderable.getRenderingProgramConfiguration(), renderable.getId());
		}
	}


	private RenderingProgrammChangeHandlerConfiguration getConfig() {
		return (RenderingProgrammChangeHandlerConfiguration) config;
	}
}
