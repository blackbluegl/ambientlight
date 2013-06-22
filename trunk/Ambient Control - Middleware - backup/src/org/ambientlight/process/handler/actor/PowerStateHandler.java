package org.ambientlight.process.handler.actor;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.entities.Token;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.room.actors.ActorConfiguration;
import org.ambientlight.room.actors.LightObjectConfiguration;
import org.ambientlight.room.actors.SwitchObjectConfiguration;
import org.ambientlight.room.entities.LightObject;


public class PowerStateHandler extends AbstractActionHandler {

	@Override
	public void performAction(Token data) {

		// if simple mode handle all actors the same way
		if (this.config instanceof SimplePowerStateHandlerConfiguration) {
			boolean powerState = ((SimplePowerStateHandlerConfiguration) config).powerState;
			for (ActorConfiguration actorConfig : AmbientControlMW.getRoom().config.actorConfigurations.values()) {
				switchPowerState(actorConfig.getName(), powerState, actorConfig);
			}
			return;
		}

		for (String currentActorName : getConfig().getActorNames()) {
			boolean powerState = getConfig().getActorPowerState(currentActorName);
			ActorConfiguration actorConfig = AmbientControlMW.getRoom().config.actorConfigurations.get(currentActorName);
			switchPowerState(currentActorName, powerState, actorConfig);
		}
	}


	/**
	 * @param currentActorName
	 */
	public void switchPowerState(String currentActorName, boolean powerState, ActorConfiguration actorConfig) {
		try {
			if (actorConfig instanceof SwitchObjectConfiguration) {
				// update switch device
				AmbientControlMW
				.getRoom()
				.getSwitchingDevice()
				.writeData(((SwitchObjectConfiguration) actorConfig).deviceType,
						((SwitchObjectConfiguration) actorConfig).houseCode,
						((SwitchObjectConfiguration) actorConfig).switchingUnitCode, powerState);

				// update model
				actorConfig.setPowerState(powerState);

			} else if (actorConfig instanceof LightObjectConfiguration) {
				// update renderer for light objects
				LightObject lightObject = AmbientControlMW.getRoom().getLightObjectByName(currentActorName);
				AmbientControlMW.getRenderProgrammFactory().updatePowerStateForLightObject(AmbientControlMW.getRenderer(),
						lightObject, powerState);
			}

			// update model
			actorConfig.setPowerState(powerState);
		} catch (Exception e) {
			System.out.println("error while trying to set Powerstate for: " + currentActorName + ". Exception was: ");
			e.printStackTrace();
		}
	}


	private PowerstateHandlerConfiguration getConfig() {
		return (PowerstateHandlerConfiguration) this.config;
	}
}
