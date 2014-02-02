package org.ambientlight.process.handler.actor;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.process.handler.actor.PowerstateHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.process.Token;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.room.entities.features.actor.types.SwitcheableId;


public class PowerStateHandler extends AbstractActionHandler {

	@Override
	public void performAction(Token data) {

		// if simple mode handle all actors the same way
		if (this.config instanceof SimplePowerStateHandlerConfiguration) {
			boolean powerState = ((SimplePowerStateHandlerConfiguration) config).powerState;

			for (SwitcheableId switchableIds : AmbientControlMW.getRoom().featureFacade.getSwitchableIds()) {
				AmbientControlMW.getRoom().featureFacade.setSwitcheablePowerState(switchableIds.type, switchableIds.id,
						powerState);
			}
			return;
		}

		for (SwitcheableId currentId : getConfig().powerStateConfiguration.keySet()) {
			boolean powerState = getConfig().powerStateConfiguration.get(currentId);
			AmbientControlMW.getRoom().featureFacade.setSwitcheablePowerState(currentId.type, currentId.id, powerState);
		}
	}


	private PowerstateHandlerConfiguration getConfig() {
		return (PowerstateHandlerConfiguration) this.config;
	}
}
