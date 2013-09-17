package org.ambientlight.webservice;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.room.IUserRoomItem;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.actors.LightObjectConfiguration;
import org.ambientlight.room.actors.SwitchObjectConfiguration;
import org.ambientlight.room.entities.LightObject;
import org.ambientlight.room.eventgenerator.EventGeneratorConfiguration;
import org.ambientlight.scenery.actor.ActorConductConfiguration;
import org.ambientlight.scenery.actor.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.scenery.actor.switching.SwitchingConfiguration;


//TODO should be better named: entitiy Control
@Path("/sceneryControl")
public class SceneryControl {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getInfo() {
		return "Version:0.11.0";
	}


	@PUT
	@Path("/config/room/items/{itemName}/program")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public synchronized Response createOrUpdateActorConductConfigurationForItem(@PathParam("itemName") String itemName,
			ActorConductConfiguration itemConfiguration) {

		System.out.println("SceneryControlWS:  setting config for " + itemName + " to " + itemConfiguration.getClass().getName());

		if (AmbientControlMW.getRoom().config.actorConfigurations.containsKey(itemName) == false) {
			System.out.println("SceneryControlWS: item does not exist or is no actor: " + itemName);
			return Response.status(500).build();
		}

		if (itemConfiguration instanceof RenderingProgramConfiguration) {
			// update renderer
			LightObject lightObject = AmbientControlMW.getRoom().getLightObjectByName(itemName);

			AmbientControlMW.getRenderProgrammFactory().updateRenderingConfigurationForLightObject(
					AmbientControlMW.getRenderer(), (RenderingProgramConfiguration) itemConfiguration, lightObject);
		} else if (itemConfiguration instanceof SwitchingConfiguration) {
			// nothing todo so far
		} else
			return Response.status(500).build();

		// update running model
		AmbientControlMW.getRoom().config.actorConfigurations.get(itemName).actorConductConfiguration = itemConfiguration;

		// persist model
		RoomConfiguration persistetConfig;
		try {
			persistetConfig = RoomConfigurationFactory.getRoomConfigByName(AmbientControlMW.getRoomConfigFileName());
			persistetConfig.actorConfigurations.get(itemName).actorConductConfiguration = itemConfiguration;
			RoomConfigurationFactory.saveRoomConfiguration(persistetConfig, AmbientControlMW.getRoomConfigFileName());
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}

		return Response.status(200).build();
	}


	@PUT
	@Path("/config/room/eventGenerator/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public synchronized Response createOrUpdateEventGeneratorConfig(@PathParam("id") String eventGeneratorName,
			EventGeneratorConfiguration config) {
		System.out.println("SceneryControl: saving eventGeneratorConfig");
		AmbientControlMW.getRoom().config.eventGeneratorConfigurations.put(eventGeneratorName, config);
		// TODO this saves the complete state. do it like in
		// updateconductconfiguration to just save the given part
		try {
			RoomConfigurationFactory.saveRoomConfiguration(AmbientControlMW.getRoom().config,
					AmbientControlMW.getRoomConfigFileName());
			System.out.println("SceneryControl: saving eventGeneratorConfig finished");
			return Response.status(200).build();

		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}


	@GET
	@Path("/config/room")
	@Produces(MediaType.APPLICATION_JSON)
	public RoomConfiguration getRoomConfiguration() {
		return AmbientControlMW.getRoom().config;
	}


	@PUT
	@Path("/control/room/items/{itemName}/state")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void setPowerStateForItem(@PathParam("itemName") String itemName, Boolean powerState) {

		System.out.println("SceneryControlWS:  setting power state for " + itemName + " to " + powerState);

		try {
			IUserRoomItem config = this.getRoomConfiguration().getUserRoomItems().get(itemName);

			if (config instanceof LightObjectConfiguration) {
				// update renderer
				AmbientControlMW.getRenderProgrammFactory().updatePowerStateForLightObject(AmbientControlMW.getRenderer(),
						AmbientControlMW.getRoom().getLightObjectByName(itemName), powerState);
			} else {
				// update switch device
				AmbientControlMW
				.getRoom()
				.getSwitchingDevice()
				.writeData(((SwitchObjectConfiguration) config).deviceType,
						((SwitchObjectConfiguration) config).houseCode,
						((SwitchObjectConfiguration) config).switchingUnitCode, powerState);
			}

			// update model
			config.setPowerState(powerState);
		} catch (IOException e) {
			e.printStackTrace();
			Response.status(500).build();
		}
	}
}