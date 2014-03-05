package org.ambientlight.process.handler.actor;

import org.ambientlight.config.process.handler.actor.RenderingProgrammChangeHandlerConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.process.Token;
import org.ambientlight.process.handler.AbstractActionHandler;


public class RenderingProgrammChangeHandler extends AbstractActionHandler {

	@Override
	public void performAction(Token data) {
		for (String currentId : getConfig().renderConfig.keySet()) {
			RenderingProgramConfiguration config = getConfig().renderConfig.get(currentId);
			featureFacade.setRenderingConfiguration(config, currentId);
		}
	}


	private RenderingProgrammChangeHandlerConfiguration getConfig() {
		return (RenderingProgrammChangeHandlerConfiguration) config;
	}
}
