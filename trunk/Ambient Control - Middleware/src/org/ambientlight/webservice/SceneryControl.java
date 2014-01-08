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
import org.ambientlight.config.features.actor.Switchable;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.config.room.entities.lightobject.ActorConductConfiguration;
import org.ambientlight.config.room.entities.lightobject.LightObjectConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.switching.SwitchingConfiguration;
import org.ambientlight.config.room.entities.remoteswitches.RemoteSwitch;
import org.ambientlight.config.room.triggers.EventGeneratorConfiguration;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.entities.lightobject.LightObject;


//TODO should be better named: entitiy Control
@Path("/sceneryControl")
public class SceneryControl {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getInfo() {
		return "Version:0.15.0";
	}


	@PUT
	@Path("/config/room/items/{itemName}/program")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public synchronized Response createOrUpdateActorConductConfigurationForItem(@PathParam("itemName") String itemName,
			ActorConductConfiguration itemConfiguration) {

		System.out.println("SceneryControlWS:  setting config for " + itemName + " to " + itemConfiguration.getClass().getName());

		if (AmbientControlMW.getRoom().config.lightObjectConfigurations.containsKey(itemName) == false) {
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

			RoomConfigurationFactory.commitTransaction();

			return Response.status(500).build();
		}

		// update running model
		AmbientControlMW.getRoom().config.lightObjectConfigurations.get(itemName).actorConductConfiguration = itemConfiguration;

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

		RoomConfigurationFactory.commitTransaction();
		System.out.println("SceneryControl: saving eventGeneratorConfig finished");

		return Response.status(200).build();
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
	public Map<String, Switchable> getUserRoomItems() {
		return AmbientControlMW.getRoom().config.getSwitchableActors();
	}


	@PUT
	@Path("/control/room/items/{itemName}/state")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void setPowerStateForItem(@PathParam("itemName") String itemName, Boolean powerState) {

		System.out.println("SceneryControlWS:  setting power state for " + itemName + " to " + powerState);

		try {
			Switchable config = this.getRoomConfiguration().getSwitchableActors().get(itemName);

			if (config instanceof LightObjectConfiguration) {
				// update renderer
				AmbientControlMW.getRenderProgrammFactory().updatePowerStateForLightObject(AmbientControlMW.getRenderer(),
						AmbientControlMW.getRoom().getLightObjectByName(itemName), powerState);
			} else {
				// update switch device
				AmbientControlMW
				.getRoom()
				.getSwitchingDevice()
				.writeData(((RemoteSwitch) config).deviceType,
						((RemoteSwitch) config).houseCode,
						((RemoteSwitch) config).switchingUnitCode, powerState);
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