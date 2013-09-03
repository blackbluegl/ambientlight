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
import org.ambientlight.scenery.AbstractSceneryConfiguration;
import org.ambientlight.ws.container.SceneriesContainer;


@Path("/sceneryControl")
public class SceneryControl {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getInfo() {
		return "Version:0.10.0\nProtocoll:0.10.0";
	}


	@GET
	@Path("/config/room/sceneries")
	@Produces(MediaType.APPLICATION_JSON)
	public AbstractSceneryConfiguration[] getSceneries() {
		return getRoomConfiguration().sceneries
				.toArray(new AbstractSceneryConfiguration[getRoomConfiguration().sceneries.size()]);
	}


	@PUT
	@Path("/config/room/sceneries")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createOrUpdateSceneryConfigurations(SceneriesContainer sceneries) {
		System.out.println("SceneryControl: saving sceneries");
		AmbientControlMW.getRoom().config.sceneries = sceneries.sceneries;

		try {
			RoomConfigurationFactory.saveRoomConfiguration(AmbientControlMW.getRoom().config,
					AmbientControlMW.getRoomConfigFileName());
			System.out.println("SceneryControl: saving sceneries finished");
			return Response.status(200).build();

		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}


	//
	// @DELETE
	// @Path("/config/room/sceneries/{name}")
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response deleteScenery(@PathParam("name") String name) {
	// System.out.println("SceneryControlWS:  deleteing scenery: " + name);
	//
	// if (getRoomConfiguration().currentScenery.equals(name))
	// return Response.status(500).build();
	// if (getSceneries().contains(name) == false)
	// return Response.status(404).build();
	//
	// for (ActorConfiguration currentRoomItemConfiguration :
	// Room.getRoomConfig().actorConfigurations) {
	// currentRoomItemConfiguration.sceneryConfigurationBySzeneryName.remove(name);
	// }
	//
	// // save config model to file
	// try {
	// RoomConfigurationFactory.saveRoomConfiguration(Room.getRoomConfig(),
	// AmbientControlMW.getRoomConfigFileName());
	// } catch (IOException e) {
	// e.printStackTrace();
	// return Response.status(500).build();
	// }
	//
	// return Response.status(200).build();
	// }
	//
	//
	@GET
	@Path("/config/room")
	@Produces(MediaType.APPLICATION_JSON)
	public RoomConfiguration getRoomConfiguration() {
		return AmbientControlMW.getRoom().config;
	}


	//
	//
	// @PUT
	// @Path("/control/room/sceneries")
	// @Consumes(MediaType.TEXT_PLAIN)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response changeRoomToScenery(String sceneryName) {
	// System.out.println("SceneryControlWS:  activating scenery: " +
	// sceneryName);
	//
	// // first preserve the old powerstates we need them later after model is
	// // already restored from file
	// Map<String, Boolean> powerStates = new HashMap<String, Boolean>();
	// for (ActorConfiguration currentRoomItemConfiguration :
	// getRoomConfiguration().actorConfigurations.values()) {
	// powerStates
	// .put(currentRoomItemConfiguration.getName(),
	// currentRoomItemConfiguration.getPowerState());
	// }
	//
	// // restore configuration from storage so we do not save old unwished
	// // configurations and save different configurations the user played with
	// // but never wanted them to be saved.
	// try {
	// Room
	// .setRoomConfig(RoomConfigurationFactory.getRoomConfigByName(AmbientControlMW.getRoomConfigFileName()));
	// } catch (IOException e) {
	// e.printStackTrace();
	// return Response.status(500).build();
	// }
	//
	// Room.getRoomConfig().currentScenery = sceneryName;
	//
	// for (ActorConfiguration currentItemConfiguration :
	// this.getRoomConfiguration().actorConfigurations.values()) {
	//
	// ActorConfiguration newSceneryConfig =
	// currentItemConfiguration.getSceneryConfigurationBySceneryName(sceneryName);
	//
	// // only switch lightObjects which are not bypassed by user -
	// // therefore preserve state of the old config and copy to the new
	// // one
	// if (newSceneryConfig.bypassOnSceneryChange) {
	// System.out.println("SceneryControlWS:  ommiting " +
	// currentItemConfiguration.name
	// + " because it is is set to bypass the scenery change");
	// newSceneryConfig.powerState =
	// powerStates.get(currentItemConfiguration.name);
	// continue;
	// }
	//
	// this.changeItemToScenery(sceneryName, currentItemConfiguration.name);
	// }
	//
	// // save config model to file
	// try {
	// RoomConfigurationFactory.saveRoomConfiguration(Room.getRoomConfig(),
	// AmbientControlMW.getRoomConfigFileName());
	// } catch (IOException e) {
	// e.printStackTrace();
	// return Response.status(500).build();
	// }
	//
	// return Response.status(200).build();
	// }
	//
	//
	// @PUT
	// @Path("/control/room/sceneries/{sceneryName}/items")
	// @Consumes(MediaType.TEXT_PLAIN)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response changeItemToScenery(@PathParam("sceneryName") String
	// sceneryName, String itemName) {
	// ActorConfiguration itemConfiguration =
	// this.getRoomConfiguration().getActorConfigurationByName(itemName);
	// ActorConfiguration newSceneryConfig =
	// itemConfiguration.getSceneryConfigurationBySceneryName(sceneryName);
	//
	// if (itemConfiguration instanceof LightObjectConfiguration) {
	// // update renderer
	// LightObject lightObject =
	// AmbientControlMW.getRoom().getLightObjectByName(itemName);
	//
	// AmbientControlMW.getRenderProgrammFactory().updateRenderingConfigurationForLightObject(
	// AmbientControlMW.getRenderer(), newSceneryConfig, lightObject);
	// } else if (itemConfiguration instanceof SwitchObjectConfiguration) {
	// // simply update the powerstate
	// this.setPowerStateForItem(itemName, newSceneryConfig.powerState);
	// } else
	// return Response.status(500).build();
	// return Response.status(200).build();
	// }
	//
	//
	// @PUT
	// @Path("/config/room/sceneries/{sceneryName}")
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response
	// createOrUpdateSceneryConfigurationForRoom(@PathParam("sceneryName")
	// String sceneryName,
	// List<RenderingProgrammConfigurationLightObjectNameMapper> configList) {
	//
	// System.out.println("SceneryControlWS:  saving as scenery with name: " +
	// sceneryName);
	//
	// // update all roomItems in model
	// for (ActorConfiguration existingRoomItemConfiguration :
	// Room.getRoomConfig().actorConfigurations) {
	//
	// // extract configuration for the current lightobject
	// ActorConfiguration newSceneryConfig = null;
	// for (RenderingProgrammConfigurationLightObjectNameMapper
	// possibleCurrentConfig : configList) {
	// if
	// (possibleCurrentConfig.lightObjectName.equals(existingRoomItemConfiguration.name))
	// {
	// newSceneryConfig = possibleCurrentConfig.config;
	// break;
	// }
	// }
	// // update model
	// existingRoomItemConfiguration.sceneryConfigurationBySzeneryName.put(sceneryName,
	// newSceneryConfig);
	// }
	//
	// // save config model to file
	// try {
	// RoomConfigurationFactory.saveRoomConfiguration(Room.getRoomConfig(),
	// AmbientControlMW.getRoomConfigFileName());
	// } catch (IOException e) {
	// e.printStackTrace();
	// Response.status(500).build();
	// }
	//
	// System.out.println("SceneryControlWS:  saved as scenery with name: " +
	// sceneryName + " done");
	//
	// return Response.status(200).build();
	// }
	//
	//

	// @PUT
	// @Path("/config/room/sceneries/{sceneryName}/items/{itemName}/program")
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response
	// createOrUpdateSceneryConfigurationForItem(@PathParam("sceneryName")
	// String sceneryName,
	// @PathParam("itemName") String itemName, ActorConfiguration
	// newSceneryConfig) {
	//
	// System.out.println("SceneryControlWS:  setting config for " + itemName +
	// " to " + newSceneryConfig.getClass().getName());
	//
	// // update model
	// ActorConfiguration roomItemConfiguration =
	// Room.getRoomConfig().getActorConfigurationByName(itemName);
	// roomItemConfiguration.sceneryConfigurationBySzeneryName.put(sceneryName,
	// newSceneryConfig);
	//
	// return Response.status(200).build();
	// }
	//
	//
	// @PUT
	// @Path("/control/room/state")
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response setPowerStateForRoom(Boolean powerState) {
	// System.out.println("SceneryControlWS:  setting power state for room to "
	// + powerState);
	//
	// if (powerState == false) {
	// for (ActorConfiguration current :
	// this.getRoomConfiguration().actorConfigurations) {
	// this.setPowerStateForItem(current.name, powerState);
	// }
	// } else {
	// changeRoomToScenery(Room.getRoomConfig().currentScenery);
	// }
	//
	// return Response.status(200).build();
	// }
	//
	//
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