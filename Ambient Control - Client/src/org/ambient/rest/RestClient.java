package org.ambient.rest;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.ambientlight.config.process.ProcessConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.events.BroadcastEvent;
import org.ambientlight.room.entities.features.actor.types.SwitchType;
import org.ambientlight.ws.Room;
import org.ambientlight.ws.process.validation.ValidationResult;


public class RestClient {

	public static Room getRoom(String roomName) throws InterruptedException, ExecutionException {
		GetRoomTask task = new GetRoomTask();
		task.execute(Rest.SERVER_NAME, roomName);
		return task.get();
	}


	public static List<String> getRoomNames() throws InterruptedException, ExecutionException {
		GetRoomTask task = new GetRoomTask();
		task.execute(Rest.SERVER_NAME);
		return (List<String>) task.get();
	}


	public static Boolean registerCallback(String roomName, String ipAndPort) throws InterruptedException, ExecutionException {
		RegisterCallbackTask task = new RegisterCallbackTask();
		task.execute(Rest.SERVER_NAME, roomName, ipAndPort);
		return task.get();
	}


	public static void unregisterCallback(String roomName, String ipAndPort) {
		UnregisterCallbackTask task = new UnregisterCallbackTask();
		task.execute(Rest.SERVER_NAME, roomName, ipAndPort);
	}


	public static void setSwitchablePowerState(String room, SwitchType type, String id, boolean powestate) {
		SetSwitchablePowerState task = new SetSwitchablePowerState();
		task.execute(Rest.SERVER_NAME, room, type, id, powestate);
	}


	public static void startProcess(String roomName, String processId) {
		StartProcessTask task = new StartProcessTask();
		task.execute(Rest.SERVER_NAME, roomName, processId);
	}


	public static void stopProcess(String roomName, String processId) {
		StopProcessTask task = new StopProcessTask();
		task.execute(Rest.SERVER_NAME, roomName, processId);
	}


	public static void deleteScenarioFromRoom(String hostName, String sceneryName) {
		DeleteSceneryTask task = new DeleteSceneryTask();
		task.execute(hostName, sceneryName);
	}


	public static void deleteProcessFromRoom(String roomName, String processName) {
		DeleteProcessTask task = new DeleteProcessTask();
		task.execute(Rest.SERVER_NAME, roomName, processName);
	}


	public static ValidationResult addProcess(String roomName, ProcessConfiguration process) throws InterruptedException,
	ExecutionException {
		AddProcessTask task = new AddProcessTask();
		task.execute(Rest.SERVER_NAME, roomName, process);
		return task.get();
	}


	public static ValidationResult validateProcess(String roomName, ProcessConfiguration process) throws InterruptedException,
	ExecutionException {
		VerifyProcessTask task = new VerifyProcessTask();
		task.execute(Rest.SERVER_NAME, roomName, process);
		return task.get();
	}


	public static void setPowerStateForRoom(String hostName, Boolean state) throws InterruptedException, ExecutionException {
		ToggleRoomPowerStateTask task = new ToggleRoomPowerStateTask();
		task.execute(hostName, state);
	}


	public static void setCurrentScenery(String hostName, String sceneryName) {
		SetCurrentSceneryTask task = new SetCurrentSceneryTask();
		task.execute(hostName, sceneryName);
	}


	public static void setRenderingConfiguration(String roomName, String itemName, RenderingProgramConfiguration config) {
		SetActorConductConfigurationTask task = new SetActorConductConfigurationTask();
		task.execute(Rest.SERVER_NAME, roomName, itemName, config);
	}


	public static void sendEvent(String hostName, BroadcastEvent event) {
		SendEventTask task = new SendEventTask();
		task.execute(hostName, event);
	}


	public static void createScenery(String roomName, String scenery) throws Exception {
		CreateSceneryTask task = new CreateSceneryTask();
		task.execute(Rest.SERVER_NAME, roomName, scenery);
	}
}
