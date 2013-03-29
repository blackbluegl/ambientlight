package org.ambientlight.scenery.ws;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientLight;
import org.ambientlight.scenery.entities.LightObject;
import org.ambientlight.scenery.entities.configuration.LightObjectConfiguration;
import org.ambientlight.scenery.entities.configuration.RoomConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.RenderingProgrammConfiguration;
import org.ambientlight.ws.container.RenderingProgrammConfigurationLightObjectNameMapper;

@Path("/sceneryControl")
public class SceneryControl {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getVersionString() {
		return "AmbientLight:0.9.1";
	}

	@GET
	@Path("/config/room/sceneries")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<String> getSceneries() {

		Set<String> result = new HashSet<String>();

		for (LightObjectConfiguration currentLightConfig : this.getRoom().lightObjects) {
			for (RenderingProgrammConfiguration currentConfig : currentLightConfig.renderingProgrammConfigurationBySzeneryName) {
				result.add(currentConfig.sceneryName);
			}
		}
		return result;
	}

	
	@GET
	@Path("/config/room")
	@Produces(MediaType.APPLICATION_JSON)
	public RoomConfiguration getRoom() {
		return AmbientLight.getRoomConfig();
	}

	
	@PUT
	@Path("/control/room/state")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setRoomPowerState(Boolean powerState) {
		System.out.println("setting power state for room to " + powerState);

		for (LightObject current : AmbientLight.getRoom().getLightObjectsInRoom()) {
			this.setLightObjectPowerState(current.getConfiguration().lightObjectName, powerState);
		}

		return Response.status(200).build();
	}

	
	@POST
	@Path("/control/room/lightObjects/{lightObjectName}/state")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public LightObjectConfiguration setLightObjectPowerState(@PathParam("lightObjectName") String lightObjectName,
			Boolean powerState) {

		System.out.println("setting power state for " + lightObjectName + " to "
				+ powerState);
		
		//update renderer
		AmbientLight.getRenderProgrammFactory().updatePowerStateForLightObject(
				AmbientLight.getRenderer(),
				AmbientLight.getRoom().getLightObjectByName(lightObjectName),
				powerState);

		//update model
		AmbientLight.getRoomConfig().getLightObjectConfigurationByName(
				lightObjectName).currentRenderingProgrammConfiguration.powerState = powerState;
		
		//save model
		try {
			AmbientLight.getRoomFactory().saveRoomConfiguration(
					AmbientLight.getRoomConfig(), "default");
		} catch (IOException e) {
			e.printStackTrace();
			Response.status(500).build();
		}
		
		return AmbientLight.getRoomConfig().getLightObjectConfigurationByName(lightObjectName);
	}

	
	@PUT
	@Path("/control/room/lightObjects/{lightObjectName}/program")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setCurrentLightObjectRenderingConfig(@PathParam("lightObjectName") String lightObjectName,
			RenderingProgrammConfiguration newConfig) {

		System.out.println("setting config for" + lightObjectName + " to "
				+ newConfig.getClass().getName());
		
		//update renderer
		LightObject lightObject = AmbientLight.getRoom().getLightObjectByName(
				lightObjectName);

		AmbientLight.getRenderProgrammFactory().updateRenderingConfigurationForLightObject(
						AmbientLight.getRenderer(), newConfig, lightObject);

		//update model
		LightObjectConfiguration modelConfig = AmbientLight.getRoomConfig()
				.getLightObjectConfigurationByName(lightObjectName);
		modelConfig.currentRenderingProgrammConfiguration = newConfig;

		//save model
		try {
			AmbientLight.getRoomFactory().saveRoomConfiguration(
					AmbientLight.getRoomConfig(), "default");
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
		
		for (LightObject currentLightObject : AmbientLight.getRoom().getLightObjectsInRoom()) {
		
			//retreiving coresponding configuration
			RenderingProgrammConfiguration newConfig = currentLightObject.getConfiguration()
					.getRenderingProgrammConfigurationBySceneryName(sceneryName);
			
			//updating rendering program
			this.setCurrentLightObjectRenderingConfig(currentLightObject.getConfiguration().lightObjectName, newConfig);
			
			//updating powerstate coresponding configuration
			this.setLightObjectPowerState(currentLightObject.getConfiguration().lightObjectName,newConfig.powerState);
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
		for (LightObjectConfiguration currentOldLightObjectConfiguration : AmbientLight.getRoomConfig().lightObjects) {
			
			//extract configuration for the current lightobject
			RenderingProgrammConfiguration newConfig = null;
			for(RenderingProgrammConfigurationLightObjectNameMapper possibleCurrentConfig : configList ){
				if(possibleCurrentConfig.lightObjectName.equals(currentOldLightObjectConfiguration.lightObjectName)){
					newConfig=possibleCurrentConfig.config;
					break;
				}
			}
			
			// get shure that this value has been initialized with the new sceneryName
			newConfig.sceneryName = sceneryName;
			
			//remove old config if existing
			RenderingProgrammConfiguration existingRenderingProgramConfiguration = currentOldLightObjectConfiguration.getRenderingProgrammConfigurationBySceneryName(sceneryName);
			
			//udating lightobject
			LightObject currentLightObject = AmbientLight.getRoom().getLightObjectByName(currentOldLightObjectConfiguration.lightObjectName);

			if(existingRenderingProgramConfiguration != null){
				//remove from model
				currentOldLightObjectConfiguration.renderingProgrammConfigurationBySzeneryName.remove(existingRenderingProgramConfiguration);
				//remove from instantiated lightobject
				currentLightObject.getConfiguration().renderingProgrammConfigurationBySzeneryName
				.remove(existingRenderingProgramConfiguration);
			}
			
			//update in config
			currentOldLightObjectConfiguration.renderingProgrammConfigurationBySzeneryName.add(newConfig);
			// update real objects
			currentLightObject.getConfiguration().renderingProgrammConfigurationBySzeneryName.add(newConfig);
		}

		// save config model to file
		try {
			AmbientLight.getRoomFactory().saveRoomConfiguration(AmbientLight.getRoomConfig(), "default");
		} catch (IOException e) {
			e.printStackTrace();
			Response.status(500).build();
		}

		System.out.println("saving as scenery with name: " + sceneryName + " done");

		return Response.status(200).build();
	}
}
