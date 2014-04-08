package org.ambientlight.process.handler.actor;

import org.ambientlight.config.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.process.Token;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.room.entities.features.EntityId;


public class SimplePowerStateHandler extends AbstractActionHandler {

	@Override
	public void performAction(Token data) {
		for (EntityId currentId : featureFacade.getSwitchableIds()) {
			featureFacade.setSwitcheablePowerState(currentId, getConfig().powerState, false);
		}
	}


	private SimplePowerStateHandlerConfiguration getConfig() {
		return (SimplePowerStateHandlerConfiguration) this.config;
	}
}
