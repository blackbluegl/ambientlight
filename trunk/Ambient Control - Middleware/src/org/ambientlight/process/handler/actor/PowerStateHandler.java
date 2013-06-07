package org.ambientlight.process.handler.actor;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.room.actors.ActorConfiguration;
import org.ambientlight.room.actors.LightObjectConfiguration;
import org.ambientlight.room.actors.SwitchObjectConfiguration;
import org.ambientlight.room.entities.LightObject;


public class PowerStateHandler extends AbstractActionHandler {

	@Override
	public void performAction(Object data) {
		for (String currentActorName : getConfig().getActorNames()) {
			try {
				boolean powerState = getConfig().getActorPowerState(currentActorName);
				ActorConfiguration actorConfig = AmbientControlMW.getRoomConfig().actorConfigurations.get(currentActorName);

				if (actorConfig instanceof SwitchObjectConfiguration) {
					// update switch device
					AmbientControlMW
							.getRoom()
							.getSwitchingDevice()
							.writeData(((SwitchObjectConfiguration) actorConfig).deviceType,
									((SwitchObjectConfiguration) actorConfig).houseCode,
									((SwitchObjectConfiguration) actorConfig).switchingUnitCode, powerState);
					
					//update model
					actorConfig.setPowerState(powerState);
					
				} else if (actorConfig instanceof LightObjectConfiguration) {
					// update renderer for light objects
					LightObject lightObject = AmbientControlMW.getRoom().getLightObjectByName(currentActorName);
					AmbientControlMW.getRenderProgrammFactory().updatePowerStateForLightObject(AmbientControlMW.getRenderer(),
							lightObject, powerState);
				}
				
				//update model
				actorConfig.setPowerState(powerState);
			} catch (Exception e) {
				System.out.println("error while trying to set Powerstate for: " + currentActorName + ". Exception was: "
						+ e.getMessage());
			}
		}
	}


	private PowerstateHandlerConfiguration getConfig() {
		return (PowerstateHandlerConfiguration) this.config;
	}
}
