package org.ambientlight.process.handler.actor;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.features.actor.Switchable;
import org.ambientlight.config.process.handler.actor.PowerstateHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.config.room.entities.lightobject.LightObjectConfiguration;
import org.ambientlight.process.Token;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.room.entities.lightobject.LightObject;
import org.ambientlight.room.entities.remoteswitches.RemoteSwitch;


public class PowerStateHandler extends AbstractActionHandler {

	@Override
	public void performAction(Token data) {

		// if simple mode handle all actors the same way
		if (this.config instanceof SimplePowerStateHandlerConfiguration) {
			boolean powerState = ((SimplePowerStateHandlerConfiguration) config).powerState;
			for (Switchable actorConfig : AmbientControlMW.getRoom().config.getSwitchableActors().values()) {
				switchPowerState(actorConfig.getId(), powerState, actorConfig);
			}
			return;
		}

		for (String currentActorName : getConfig().powerStateConfiguration.keySet()) {
			boolean powerState = getConfig().powerStateConfiguration.get(currentActorName);
			Switchable actorConfig = AmbientControlMW.getRoom().config.getSwitchableActors().get(currentActorName);
			switchPowerState(currentActorName, powerState, actorConfig);
		}
	}


	/**
	 * @param currentActorName
	 */
	public void switchPowerState(String currentActorName, boolean powerState, Switchable actorConfig) {
		try {
			if (actorConfig instanceof RemoteSwitch) {
				// update switch device
				AmbientControlMW
				.getRoom()
				.getSwitchingDevice()
				.setState(((RemoteSwitch) actorConfig).deviceType,
						((RemoteSwitch) actorConfig).houseCode,
						((RemoteSwitch) actorConfig).switchingUnitCode, powerState);

			} else if (actorConfig instanceof LightObjectConfiguration) {
				// update renderer for light objects and update model
				LightObject lightObject = AmbientControlMW.getRoom().getLightObjectByName(currentActorName);
				AmbientControlMW.getRenderProgrammFactory().setPowerStateForLightObject(AmbientControlMW.getRenderer(),
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
