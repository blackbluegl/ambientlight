package org.ambientlight.process.handler.actor;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.process.handler.actor.ConfigurationChangeHandlerConfiguration;
import org.ambientlight.config.room.entities.lightobject.ActorConductConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.process.entities.Token;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.room.entities.lightobject.LightObject;


public class ConfigurationChangeHandler extends AbstractActionHandler {

	@Override
	public void performAction(Token data) {
		for (String currentActorName : getConfig().actorConfiguration.keySet()) {
			ActorConductConfiguration config = getConfig().actorConfiguration.get(currentActorName);

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
