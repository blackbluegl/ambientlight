package org.ambient.control.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.ambientlight.scenery.entities.configuration.LightObjectConfiguration;
import org.ambientlight.scenery.entities.configuration.RoomConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.RenderingProgrammConfiguration;
import org.ambientlight.ws.container.RenderingProgrammConfigurationLightObjectNameMapper;

public  class RestClient {

	
	public static RoomConfiguration getRoom(String hostName) throws InterruptedException, ExecutionException{
		GetRoomTask task = new GetRoomTask();
		task.execute(hostName);
		return task.get();
	}
	
	public static String[] getSceneriesForRoom(String hostName) throws InterruptedException, ExecutionException{
		GetSceneriesTask task = new GetSceneriesTask();
		task.execute(hostName);
		return task.get();
	}
	
	public static void setPowerStateForRoom(String hostName, Boolean state) throws InterruptedException, ExecutionException{
		ToggleRoomPowerStateTask task = new ToggleRoomPowerStateTask();
		task.execute(hostName,state);
	}
	
	public static LightObjectConfiguration setPowerStateForLightObject(String hostName, String lightObject, Boolean state) throws InterruptedException, ExecutionException{
		ToggleLightObjectPowerStateTask task = new ToggleLightObjectPowerStateTask();
		task.execute(hostName,lightObject, state);
		return task.get();
	}

	public static void setSceneryActive(String hostName,
			String sceneryName) {
		SetSceneryActiveForRoomTask task = new SetSceneryActiveForRoomTask();
		task.execute(hostName,sceneryName);
	}

	public static void setProgramForLightObject(String hostName,
			String lightObject, RenderingProgrammConfiguration config) {
			SetLightObjectConfigurationTask task = new SetLightObjectConfigurationTask();
			task.execute(hostName,lightObject,config);
	}

	public static void createSceneryFromCurrent(String hostName,String newSceneryName) throws InterruptedException, ExecutionException {
		RoomConfiguration currentRoomConfig = RestClient.getRoom(hostName);
		
		List<RenderingProgrammConfigurationLightObjectNameMapper> newLightObjectConfigForScenery = new ArrayList<RenderingProgrammConfigurationLightObjectNameMapper>();
		
		for(LightObjectConfiguration currentLightObjectConfiguration : currentRoomConfig.lightObjects){
			RenderingProgrammConfigurationLightObjectNameMapper newConfigMapper = new RenderingProgrammConfigurationLightObjectNameMapper();
			newConfigMapper.lightObjectName=currentLightObjectConfiguration.lightObjectName;
			newConfigMapper.config=currentLightObjectConfiguration.currentRenderingProgrammConfiguration;
			newLightObjectConfigForScenery.add(newConfigMapper);
		}
		
		CreateSceneryFromCurrentForRoomTask task = new CreateSceneryFromCurrentForRoomTask();
		task.execute(hostName,newSceneryName,newLightObjectConfigForScenery);
	}
}
