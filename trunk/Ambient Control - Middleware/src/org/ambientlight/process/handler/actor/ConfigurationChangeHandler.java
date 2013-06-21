package org.ambientlight.process.handler.actor;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.entities.Token;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.room.entities.LightObject;
import org.ambientlight.scenery.actor.ActorConductConfiguration;
import org.ambientlight.scenery.actor.renderingprogram.RenderingProgramConfiguration;


public class ConfigurationChangeHandler extends AbstractActionHandler {

	@Override
	public void performAction(Token data) {
		for (String currentActorName : getConfig().getActorNames()) {
			ActorConductConfiguration config = getConfig().getActorConfiguration(currentActorName);

			if (config instanceof RenderingProgramConfiguration) {
				LightObject lightObject = AmbientControlMW.getRoom().getLightObjectByName(currentActorName);
				AmbientControlMW.getRenderProgrammFactory().updateRenderingConfigurationForLightObject(
						AmbientControlMW.getRenderer(), (RenderingProgramConfiguration) config, lightObject);
			}
		}
	}


	private ConfigurationChangeHandlerConfiguration getConfig() {
		return (ConfigurationChangeHandlerConfiguration) config;
	}
}
