package org.ambient.rest;

import java.util.concurrent.ExecutionException;

import org.ambientlight.config.process.ProcessConfiguration;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.events.BroadcastEvent;
import org.ambientlight.ws.process.validation.ValidationResult;


public class RestClient {

	public static RoomConfiguration getRoom(String hostName) throws InterruptedException, ExecutionException {
		GetRoomTask task = new GetRoomTask();
		task.execute(hostName);
		return (RoomConfiguration) task.get();
	}


	public static Boolean registerCallback(String hostName, String ipAndPort) throws InterruptedException, ExecutionException {
		RegisterCallbackTask task = new RegisterCallbackTask();
		task.execute(hostName, ipAndPort);
		return task.get();
	}


	public static void unregisterCallback(String hostName, String ipAndPort) {
		UnregisterCallbackTask task = new UnregisterCallbackTask();
		task.execute(hostName, ipAndPort);
	}


	public static void startProcess(String hostName, String processId) {
		StartProcessTask task = new StartProcessTask();
		task.execute(hostName, processId);
	}


	public static void stopProcess(String hostName, String processId) {
		StopProcessTask task = new StopProcessTask();
		task.execute(hostName, processId);
	}


	public void deleteScenarioFromRoom(String hostName, String sceneryName) {
		DeleteSceneryTask task = new DeleteSceneryTask();
		task.execute(hostName, sceneryName);
	}


	public static void deleteProcessFromRoom(String hostName, String processName) {
		DeleteProcessTask task = new DeleteProcessTask();
		task.execute(hostName, processName);
	}


	public static ValidationResult addProcess(String hostName, ProcessConfiguration process) throws InterruptedException,
	ExecutionException {
		AddProcessTask task = new AddProcessTask();
		task.execute(hostName, process);
		return task.get();
	}


	public static ValidationResult validateProcess(String hostName, ProcessConfiguration process) throws InterruptedException,
	ExecutionException {
		VerifyProcessTask task = new VerifyProcessTask();
		task.execute(hostName, process);
		return task.get();
	}


	public static void setPowerStateForRoom(String hostName, Boolean state) throws InterruptedException, ExecutionException {
		ToggleRoomPowerStateTask task = new ToggleRoomPowerStateTask();
		task.execute(hostName, state);
	}


	public static void setPowerStateForRoomItem(String hostName, String itemName, Boolean state) throws InterruptedException,
	ExecutionException {
		ToggleRoomItemPowerStateTask task = new ToggleRoomItemPowerStateTask();
		task.execute(hostName, itemName, state);
	}


	public void setSceneryActive(String hostName, String sceneryName) {
		SetSceneryActiveForRoomTask task = new SetSceneryActiveForRoomTask();
		task.execute(hostName, sceneryName);
	}


	public static void setRenderingConfiguration(String hostName, String itemName, RenderingProgramConfiguration config) {
		SetActorConductConfigurationTask task = new SetActorConductConfigurationTask();
		task.execute(hostName, itemName, config);
	}


	public static void sendEvent(String hostName, BroadcastEvent event) {
		SendEventTask task = new SendEventTask();
		task.execute(hostName, event);
	}


	public static void createScenery(String hostName, String scenery)
			throws Exception {
		CreateSceneryTask task = new CreateSceneryTask();
		task.execute(hostName, scenery);
	}
}
