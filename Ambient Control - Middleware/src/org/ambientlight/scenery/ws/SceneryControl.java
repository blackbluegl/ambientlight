package org.ambientlight.scenery.ws;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
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
import org.ambientlight.ws.container.RenderingProgrammConfigurationLightObjectNameMapper;

@Path("/sceneryControl")
public class SceneryControl {

	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getVersionString() {
		return "AmbientLight:0.9.3";
	}

	
	@GET
	@Path("/config/room/sceneries")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<String> getSceneries() {

		Set<String> result = new HashSet<String>();
		for (RoomItemConfiguration currentRoomItemConfiguration : AmbientControlMW.getRoomConfig().roomItemConfigurations) {
			for (SceneryConfiguration currentConfig : currentRoomItemConfiguration.sceneryConfigurationBySzeneryName) {
				result.add(currentConfig.sceneryName);
			}
		}
		
		return result;
	}

	
	@GET
	@Path("/config/room")
	@Produces(MediaType.APPLICATION_JSON)
	public RoomConfiguration getRoom() {
		return AmbientControlMW.getRoomConfig();
	}

	
	@PUT
	@Path("/control/room/state")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setRoomPowerState(Boolean powerState) {
		System.out.println("setting power state for room to " + powerState);
		
		for(RoomItemConfiguration current : this.getRoom().roomItemConfigurations){
			this.setRoomItemPowerState(current.name, powerState);
		}

		return Response.status(200).build();
	}

	
	@PUT
	@Path("/control/room/lightObjects/{lightObjectName}/program")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setCurrentLightObjectRenderingConfig(@PathParam("lightObjectName") String lightObjectName,
			SceneryConfiguration newConfig) {

		System.out.println("setting config for" + lightObjectName + " to "
				+ newConfig.getClass().getName());
		
		//update renderer
		LightObject lightObject = AmbientControlMW.getRoom().getLightObjectByName(
				lightObjectName);

		AmbientControlMW.getRenderProgrammFactory().updateRenderingConfigurationForLightObject(
						AmbientControlMW.getRenderer(), newConfig, lightObject);

		//update model
		RoomItemConfiguration modelConfig = AmbientControlMW.getRoomConfig()
				.getRoomItemConfigurationByName(lightObjectName);
		modelConfig.currentSceneryConfiguration = newConfig;

		//save model
		try {
			AmbientControlMW.getRoomFactory().saveRoomConfiguration(
					AmbientControlMW.getRoomConfig(), "default");
		} catch (IOException e) {
			e.printStackTrace();
			Response.status(500).build();
		}
		
		return Response.status(200).build();
	}

	
	@PUT
	@Path("/control/room/sceneries")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setActiveScenery(String sceneryName) {
		System.out.println("activating scenery: " + sceneryName);
		
		for(RoomItemConfiguration currentItemConfiguration : this.getRoom().roomItemConfigurations){
			//retreiving coresponding configuration
			SceneryConfiguration newSceneryConfig = currentItemConfiguration.getSceneryConfigurationBySceneryName(sceneryName);

			//only switch lightObjects which are not bypassed by user
			if(newSceneryConfig.bypassOnSceneryChange){
				System.out.println("ommiting "+currentItemConfiguration.name+" because it is is set to bypass the scenery change");
				continue;
			}
			
			if(currentItemConfiguration instanceof SwitchObjectConfiguration){
				//updating switch configuration
				currentItemConfiguration.currentSceneryConfiguration= newSceneryConfig;
			}
			
			if(currentItemConfiguration instanceof LightObjectConfiguration){
				//updating rendering program
				this.setCurrentLightObjectRenderingConfig(currentItemConfiguration.name, newSceneryConfig);
			}
			
			this.setRoomItemPowerState(currentItemConfiguration.name, newSceneryConfig.powerState);
		}

		return Response.status(200).build();
	}

	
	@PUT
	@Path("/config/room/sceneries/{sceneryName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createOrUpdateScenery(@PathParam( "sceneryName" )String sceneryName,
			List<RenderingProgrammConfigurationLightObjectNameMapper> configList) {
		
		System.out.println("saving as scenery with name: " + sceneryName);

		// update all lightobjects
		for (RoomItemConfiguration currentOldLightObjectConfiguration : AmbientControlMW.getRoomConfig().roomItemConfigurations) {
			if(currentOldLightObjectConfiguration instanceof LightObjectConfiguration){
			//extract configuration for the current lightobject
			SceneryConfiguration newConfig = null;
			for(RenderingProgrammConfigurationLightObjectNameMapper possibleCurrentConfig : configList ){
				if(possibleCurrentConfig.lightObjectName.equals(currentOldLightObjectConfiguration.name)){
					newConfig=possibleCurrentConfig.config;
					break;
				}
			}
			
			// get shure that this value has been initialized with the new sceneryName
			newConfig.sceneryName = sceneryName;
			
			//remove old config if existing
			SceneryConfiguration existingRenderingProgramConfiguration = currentOldLightObjectConfiguration.getSceneryConfigurationBySceneryName(sceneryName);
			
			//udating lightobject
			LightObject currentLightObject = AmbientControlMW.getRoom().getLightObjectByName(currentOldLightObjectConfiguration.name);

			if(existingRenderingProgramConfiguration != null){
				//remove from model
				currentOldLightObjectConfiguration.sceneryConfigurationBySzeneryName.remove(existingRenderingProgramConfiguration);
				//remove from instantiated lightobject
				currentLightObject.getConfiguration().sceneryConfigurationBySzeneryName
				.remove(existingRenderingProgramConfiguration);
			}
			
			//update in config
			currentOldLightObjectConfiguration.sceneryConfigurationBySzeneryName.add(newConfig);
			// update real objects
			currentLightObject.getConfiguration().sceneryConfigurationBySzeneryName.add(newConfig);
			}
		}

		// save config model to file
		try {
			AmbientControlMW.getRoomFactory().saveRoomConfiguration(AmbientControlMW.getRoomConfig(), "default");
		} catch (IOException e) {
			e.printStackTrace();
			Response.status(500).build();
		}

		System.out.println("saving as scenery with name: " + sceneryName + " done");

		return Response.status(200).build();
	}
	
	
	@PUT
	@Path("/control/room/items/{itemName}/state")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void setRoomItemPowerState(@PathParam("itemName") String itemName, Boolean powerState) {

		System.out.println("setting power state for " + itemName + " to " + powerState);
	
		try {
			RoomItemConfiguration config = this.getRoom().getRoomItemConfigurationByName(itemName);

			if (config instanceof LightObjectConfiguration) {
				// update renderer
				AmbientControlMW.getRenderProgrammFactory().updatePowerStateForLightObject(AmbientControlMW.getRenderer(),
						AmbientControlMW.getRoom().getLightObjectByName(itemName), powerState);
			} else {
				//update switch device
				AmbientControlMW.getRoom().getSwitchingDevice().writeData(((SwitchObjectConfiguration) config).deviceType,
								((SwitchObjectConfiguration) config).houseCode,
								((SwitchObjectConfiguration) config).switchingUnitCode, powerState);
			}

			// update model
			AmbientControlMW.getRoomConfig().getRoomItemConfigurationByName(itemName).
				currentSceneryConfiguration.powerState = powerState;

			// save model
			AmbientControlMW.getRoomFactory().saveRoomConfiguration(AmbientControlMW.getRoomConfig(), "default");
		} catch (IOException e) {
			e.printStackTrace();
			Response.status(500).build();
		}
	}
}