package org.ambientlight.scenery.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.LightObjectConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;
import org.ambientlight.room.objects.SwitchObjectConfiguration;
import org.ambientlight.scenery.SceneryConfiguration;
import org.ambientlight.scenery.entities.LightObject;
import org.ambientlight.scenery.entities.RoomConfigurationFactory;
import org.ambientlight.ws.container.RenderingProgrammConfigurationLightObjectNameMapper;


@Path("/sceneryControl")
public class SceneryControl {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getInfo() {
		return "AmbientLight:0.9.5";
	}


	@GET
	@Path("/config/room/sceneries")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<String> getSceneries() {

		Set<String> result = new HashSet<String>();
		for (RoomItemConfiguration currentRoomItemConfiguration : AmbientControlMW.getRoomConfig().roomItemConfigurations) {
			for (String sceneryName : currentRoomItemConfiguration.getSupportedSceneries()) {
				result.add(sceneryName);
			}
		}

		return result;
	}


	@DELETE
	@Path("/config/room/sceneries/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteScenery(@PathParam("name") String name) {
		System.out.println("SceneryControlWS:  deleteing scenery: " + name);

		if (getRoomConfiguration().currentScenery.equals(name)) {
			return Response.status(500).build();
		}
		if (getSceneries().contains(name) == false) {
			return Response.status(404).build();
		}

		for (RoomItemConfiguration currentRoomItemConfiguration : AmbientControlMW.getRoomConfig().roomItemConfigurations) {
			currentRoomItemConfiguration.sceneryConfigurationBySzeneryName.remove(name);
		}

		// save config model to file
		try {
			RoomConfigurationFactory.saveRoomConfiguration(AmbientControlMW.getRoomConfig(),
					AmbientControlMW.getRoomConfigFileName());
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(500).build();
		}

		return Response.status(200).build();
	}


	@GET
	@Path("/config/room")
	@Produces(MediaType.APPLICATION_JSON)
	public RoomConfiguration getRoomConfiguration() {
		return AmbientControlMW.getRoomConfig();
	}


	@PUT
	@Path("/control/room/sceneries")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response changeRoomToScenery(String sceneryName) {
		System.out.println("SceneryControlWS:  activating scenery: " + sceneryName);

		// first preserve the old powerstates we need them later after model is
		// already restored from file
		Map<String, Boolean> powerStates = new HashMap<String, Boolean>();
		for (RoomItemConfiguration currentRoomItemConfiguration : getRoomConfiguration().roomItemConfigurations) {
			powerStates
					.put(currentRoomItemConfiguration.name, currentRoomItemConfiguration
							.getSceneryConfigurationBySceneryName(getRoomConfiguration().currentScenery).powerState);
		}

		// restore configuration from storage so we do not save old unwished
		// configurations and save different configurations the user played with
		// but never wanted them to be saved.
		try {
			AmbientControlMW
					.setRoomConfig(RoomConfigurationFactory.getRoomConfigByName(AmbientControlMW.getRoomConfigFileName()));
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(500).build();
		}

		AmbientControlMW.getRoomConfig().currentScenery = sceneryName;

		for (RoomItemConfiguration currentItemConfiguration : this.getRoomConfiguration().roomItemConfigurations) {

			SceneryConfiguration newSceneryConfig = currentItemConfiguration.getSceneryConfigurationBySceneryName(sceneryName);

			// only switch lightObjects which are not bypassed by user -
			// therefore preserve state of the old config and copy to the new
			// one
			if (newSceneryConfig.bypassOnSceneryChange) {
				System.out.println("SceneryControlWS:  ommiting " + currentItemConfiguration.name
						+ " because it is is set to bypass the scenery change");
				newSceneryConfig.powerState = powerStates.get(currentItemConfiguration.name);
				continue;
			}

			this.changeItemToScenery(sceneryName, currentItemConfiguration.name);
		}

		// save config model to file
		try {
			RoomConfigurationFactory.saveRoomConfiguration(AmbientControlMW.getRoomConfig(),
					AmbientControlMW.getRoomConfigFileName());
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(500).build();
		}

		return Response.status(200).build();
	}


	@PUT
	@Path("/control/room/sceneries/{sceneryName}/items")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response changeItemToScenery(@PathParam("sceneryName") String sceneryName, String itemName) {
		RoomItemConfiguration itemConfiguration = this.getRoomConfiguration().getRoomItemConfigurationByName(itemName);
		SceneryConfiguration newSceneryConfig = itemConfiguration.getSceneryConfigurationBySceneryName(sceneryName);

		if (itemConfiguration instanceof LightObjectConfiguration) {
			// update renderer
			LightObject lightObject = AmbientControlMW.getRoom().getLightObjectByName(itemName);

			AmbientControlMW.getRenderProgrammFactory().updateRenderingConfigurationForLightObject(
					AmbientControlMW.getRenderer(), newSceneryConfig, lightObject);
		} else if (itemConfiguration instanceof SwitchObjectConfiguration) {
			// simply update the powerstate
			this.setPowerStateForItem(itemName, newSceneryConfig.powerState);
		} else {
			return Response.status(500).build();
		}
		return Response.status(200).build();
	}


	@PUT
	@Path("/config/room/sceneries/{sceneryName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createOrUpdateSceneryConfigurationForRoom(@PathParam("sceneryName") String sceneryName,
			List<RenderingProgrammConfigurationLightObjectNameMapper> configList) {

		System.out.println("SceneryControlWS:  saving as scenery with name: " + sceneryName);

		// update all roomItems in model
		for (RoomItemConfiguration existingRoomItemConfiguration : AmbientControlMW.getRoomConfig().roomItemConfigurations) {

			// extract configuration for the current lightobject
			SceneryConfiguration newSceneryConfig = null;
			for (RenderingProgrammConfigurationLightObjectNameMapper possibleCurrentConfig : configList) {
				if (possibleCurrentConfig.lightObjectName.equals(existingRoomItemConfiguration.name)) {
					newSceneryConfig = possibleCurrentConfig.config;
					break;
				}
			}
			// update model
			existingRoomItemConfiguration.sceneryConfigurationBySzeneryName.put(sceneryName, newSceneryConfig);
		}

		// save config model to file
		try {
			RoomConfigurationFactory.saveRoomConfiguration(AmbientControlMW.getRoomConfig(),
					AmbientControlMW.getRoomConfigFileName());
		} catch (IOException e) {
			e.printStackTrace();
			Response.status(500).build();
		}

		System.out.println("SceneryControlWS:  saved as scenery with name: " + sceneryName + " done");

		return Response.status(200).build();
	}


	@PUT
	@Path("/config/room/sceneries/{sceneryName}/items/{itemName}/program")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createOrUpdateSceneryConfigurationForItem(@PathParam("sceneryName") String sceneryName,
			@PathParam("itemName") String itemName, SceneryConfiguration newSceneryConfig) {

		System.out.println("SceneryControlWS:  setting config for " + itemName + " to " + newSceneryConfig.getClass().getName());

		// update model
		RoomItemConfiguration roomItemConfiguration = AmbientControlMW.getRoomConfig().getRoomItemConfigurationByName(itemName);
		roomItemConfiguration.sceneryConfigurationBySzeneryName.put(sceneryName, newSceneryConfig);

		return Response.status(200).build();
	}


	@PUT
	@Path("/control/room/state")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setPowerStateForRoom(Boolean powerState) {
		System.out.println("SceneryControlWS:  setting power state for room to " + powerState);

		for (RoomItemConfiguration current : this.getRoomConfiguration().roomItemConfigurations) {
			this.setPowerStateForItem(current.name, powerState);
		}

		return Response.status(200).build();
	}


	@PUT
	@Path("/control/room/items/{itemName}/state")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void setPowerStateForItem(@PathParam("itemName") String itemName, Boolean powerState) {

		String currentScenery = AmbientControlMW.getRoomConfig().currentScenery;

		System.out.println("SceneryControlWS:  setting power state for " + itemName + " to " + powerState);

		try {
			RoomItemConfiguration config = this.getRoomConfiguration().getRoomItemConfigurationByName(itemName);

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
			AmbientControlMW.getRoomConfig().getRoomItemConfigurationByName(itemName)
					.getSceneryConfigurationBySceneryName(currentScenery).powerState = powerState;

		} catch (IOException e) {
			e.printStackTrace();
			Response.status(500).build();
		}
	}
}