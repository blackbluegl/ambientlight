package org.ambient.control.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.ambient.control.home.HomeRefreshCallback;
import org.ambient.util.GuiUtils;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;
import org.ambientlight.scenery.EntitiyConfiguration;
import org.ambientlight.ws.container.RenderingProgrammConfigurationLightObjectNameMapper;

import android.content.Context;


public class RestClient {

	public static RoomConfiguration getRoom(String hostName) throws Exception {
			GetRoomTask task = new GetRoomTask();
			task.execute(hostName);
			return (RoomConfiguration) task.get();
	}

	public static void deleteScenarioFromRoom(String hostName, String sceneryName){
		DeleteSceneryTask task = new DeleteSceneryTask();
		task.execute(hostName, sceneryName);
	}

	public static String[] getSceneriesForRoom(String hostName) throws InterruptedException, ExecutionException {
		GetSceneriesTask task = new GetSceneriesTask();
		task.execute(hostName);
		return task.get();
	}


	public static void setPowerStateForRoom(String hostName, Boolean state, HomeRefreshCallback callback) throws InterruptedException, ExecutionException {
		ToggleRoomPowerStateTask task = new ToggleRoomPowerStateTask();
		task.execute(hostName, state, callback);
	}


	public static void setPowerStateForRoomItem(String hostName, String itemName, Boolean state ) throws InterruptedException,
			ExecutionException {
		ToggleRoomItemPowerStateTask task = new ToggleRoomItemPowerStateTask();
		task.execute(hostName, itemName, state);
	}


	public static void setSceneryActive(String hostName, String sceneryName, HomeRefreshCallback callback) {
		SetSceneryActiveForRoomTask task = new SetSceneryActiveForRoomTask();
		task.execute(hostName, sceneryName, callback);
	}


	public static void setProgramForLightObject(String hostName, String sceneryName, String itemName, EntitiyConfiguration config) {
		SetLightObjectConfigurationTask task = new SetLightObjectConfigurationTask();
		task.execute(hostName, sceneryName, itemName, config);
	}


	public static void createOrUpdateSceneryFromCurrentScenery(String hostName, String newSceneryName, Context ct)
			throws Exception {
		RoomConfiguration existingRoomConfiguration = RestClient.getRoom(hostName);

		List<RenderingProgrammConfigurationLightObjectNameMapper> newLightObjectConfigForScenery = new ArrayList<RenderingProgrammConfigurationLightObjectNameMapper>();

		for (RoomItemConfiguration currentRoomItemConfiguration : existingRoomConfiguration.roomItemConfigurations) {
			RenderingProgrammConfigurationLightObjectNameMapper newConfigMapper = new RenderingProgrammConfigurationLightObjectNameMapper();

			newConfigMapper.lightObjectName = currentRoomItemConfiguration.name;
			newConfigMapper.config = currentRoomItemConfiguration
					.getSceneryConfigurationBySceneryName(existingRoomConfiguration.currentScenery);

			newLightObjectConfigForScenery.add(newConfigMapper);
		}

		CreateSceneryFromCurrentForRoomTask task = new CreateSceneryFromCurrentForRoomTask();
		task.execute(hostName, newSceneryName, newLightObjectConfigForScenery);
	}
}
