package org.ambientlight.webservice;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.room.ISwitchableRoomItem;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.config.room.actors.LightObjectConfiguration;
import org.ambientlight.config.room.actors.SwitchObjectConfiguration;
import org.ambientlight.config.room.eventgenerator.EventGeneratorConfiguration;
import org.ambientlight.config.scenery.actor.ActorConductConfiguration;
import org.ambientlight.config.scenery.actor.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.config.scenery.actor.switching.SwitchingConfiguration;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.entities.LightObject;


//TODO should be better named: entitiy Control
@Path("/sceneryControl")
public class SceneryControl {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getInfo() {
		return "Version:0.14.0";
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

		RoomConfigurationFactory.beginTransaction();

		if (itemConfiguration instanceof RenderingProgramConfiguration) {
			// update renderer
			LightObject lightObject = AmbientControlMW.getRoom().getLightObjectByName(itemName);

			AmbientControlMW.getRenderProgrammFactory().updateRenderingConfigurationForLightObject(
					AmbientControlMW.getRenderer(), (RenderingProgramConfiguration) itemConfiguration, lightObject);
		} else if (itemConfiguration instanceof SwitchingConfiguration) {
			// nothing todo so far
		} else {
			try {
				RoomConfigurationFactory.commitTransaction();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return Response.status(500).build();
		}

		// update running model
		AmbientControlMW.getRoom().config.actorConfigurations.get(itemName).actorConductConfiguration = itemConfiguration;

		// persist model
		// RoomConfiguration persistetConfig;
		try {
			// persistetConfig =
			// RoomConfigurationFactory.getRoomConfigByName(AmbientControlMW.getRoomConfigFileName());
			// persistetConfig.actorConfigurations.get(itemName).actorConductConfiguration
			// = itemConfiguration;
			// RoomConfigurationFactory.saveRoomConfiguration(persistetConfig,
			// AmbientControlMW.getRoomConfigFileName());
			RoomConfigurationFactory.commitTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
		return Response.status(200).build();
	}


	@PUT
	@Path("/config/room/eventGenerator/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public synchronized Response createOrUpdateEventGeneratorConfig(@PathParam("id") String eventGeneratorName,
			EventGeneratorConfiguration config) {
		System.out.println("SceneryControl: saving eventGeneratorConfig");
		RoomConfigurationFactory.beginTransaction();
		AmbientControlMW.getRoom().config.eventGeneratorConfigurations.put(eventGeneratorName, config);
		try {
			RoomConfigurationFactory.commitTransaction();
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


	@GET
	@Path("/config/userRoomItems")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ISwitchableRoomItem> getUserRoomItems() {
		return AmbientControlMW.getRoom().config.getUserRoomItems();
	}

	@PUT
	@Path("/control/room/items/{itemName}/state")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void setPowerStateForItem(@PathParam("itemName") String itemName, Boolean powerState) {

		System.out.println("SceneryControlWS:  setting power state for " + itemName + " to " + powerState);

		try {
			ISwitchableRoomItem config = this.getRoomConfiguration().getUserRoomItems().get(itemName);

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
			AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
		} catch (IOException e) {
			e.printStackTrace();
			Response.status(500).build();
		}
	}
}