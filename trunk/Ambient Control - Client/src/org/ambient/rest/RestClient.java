package org.ambient.rest;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.ambientlight.config.process.ProcessConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.events.BroadcastEvent;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.ws.Room;
import org.ambientlight.ws.process.validation.ValidationResult;


public class RestClient {

	public static Room getRoom(String roomName) throws InterruptedException, ExecutionException {
		GetRoomTask task = new GetRoomTask();
		task.execute(roomName);
		return task.get();
	}


	public static List<String> getRoomNames() throws InterruptedException, ExecutionException {
		GetRoomNamesTask task = new GetRoomNamesTask();
		task.execute();
		return task.get();
	}


	public static Boolean registerCallback(String roomName, String ipAndPort) throws InterruptedException, ExecutionException {
		RegisterCallbackTask task = new RegisterCallbackTask();
		task.execute(roomName, ipAndPort);
		return task.get();
	}


	public static void unregisterCallback(String roomName, String ipAndPort) {
		UnregisterCallbackTask task = new UnregisterCallbackTask();
		task.execute(roomName, ipAndPort);
	}


	public static void setSwitchablePowerState(String room, EntityId id, boolean powestate) {
		SetSwitchablePowerState task = new SetSwitchablePowerState();
		task.execute(room, id, powestate);
	}


	public static void startProcess(String roomName, String processId) {
		StartProcessTask task = new StartProcessTask();
		task.execute(roomName, processId);
	}


	public static void stopProcess(String roomName, String processId) {
		StopProcessTask task = new StopProcessTask();
		task.execute(roomName, processId);
	}


	public static void deleteScenarioFromRoom(String hostName, String sceneryName) {
		DeleteSceneryTask task = new DeleteSceneryTask();
		task.execute(hostName, sceneryName);
	}


	public static void deleteProcessFromRoom(String roomName, String processName) {
		DeleteProcessTask task = new DeleteProcessTask();
		task.execute(roomName, processName);
	}


	public static ValidationResult addProcess(String roomName, ProcessConfiguration process) throws InterruptedException,
	ExecutionException {
		AddProcessTask task = new AddProcessTask();
		task.execute(roomName, process);
		return task.get();
	}


	public static ValidationResult validateProcess(String roomName, ProcessConfiguration process) throws InterruptedException,
	ExecutionException {
		VerifyProcessTask task = new VerifyProcessTask();
		task.execute(roomName, process);
		return task.get();
	}


	public static void setPowerStateForRoom(String roomName, Boolean state) throws InterruptedException, ExecutionException {
		ToggleRoomPowerStateTask task = new ToggleRoomPowerStateTask();
		task.execute(roomName, state);
	}


	public static void setCurrentScenery(String roomName, String sceneryName) {
		SetCurrentSceneryTask task = new SetCurrentSceneryTask();
		task.execute(roomName, sceneryName);
	}


	public static void setRenderingConfiguration(String roomName, EntityId itemId, RenderingProgramConfiguration config) {
		SetRenderingConfigurationTask task = new SetRenderingConfigurationTask();
		task.execute(roomName, itemId, config);
	}


	public static void sendEvent(String roomName, BroadcastEvent event) {
		SendEventTask task = new SendEventTask();
		task.execute(roomName, event);
	}


	public static void createScenery(String roomName, String scenery) throws Exception {
		CreateSceneryTask task = new CreateSceneryTask();
		task.execute(roomName, scenery);
	}
}
