package org.ambient.rest;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.ambientlight.config.process.ProcessConfiguration;
import org.ambientlight.config.room.entities.climate.TemperaturMode;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
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
		StartStopProcessTask task = new StartStopProcessTask();
		task.execute(roomName, processId, true);
	}


	public static void stopProcess(String roomName, String processId) {
		StartStopProcessTask task = new StartStopProcessTask();
		task.execute(roomName, processId, false);
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


	public static void setCurrentScenery(String roomName, String sceneryName) {
		SetCurrentSceneryTask task = new SetCurrentSceneryTask();
		task.execute(roomName, sceneryName);
	}


	public static void setRenderingConfiguration(String roomName, EntityId itemId, RenderingProgramConfiguration config) {
		SetRenderingConfigurationTask task = new SetRenderingConfigurationTask();
		task.execute(roomName, itemId, config);
	}


	public static void createScenery(String roomName, String scenery) {
		CreateSceneryTask task = new CreateSceneryTask();
		task.execute(roomName, scenery);
	}


	public static void deleteScenery(String roomName, String sceneryName) {
		DeleteSceneryTask task = new DeleteSceneryTask();
		task.execute(roomName, sceneryName);
	}


	public static void setTemperatureMode(String roomName, TemperaturMode mode) {
		SetCurrentClimateModeTask task = new SetCurrentClimateModeTask();
		task.execute(roomName, mode);
	}


	public static void setClimateBoostMode(String roomName, boolean enable) {
		SetClimateBoostModeTask task = new SetClimateBoostModeTask();
		task.execute(roomName, enable);
	}


	/**
	 * @param roomName
	 * @param selectedProfile
	 */
	public static void setCurrentClimateProfile(String roomName, String selectedProfile) {
		SetCurrentClimateProfileTask task = new SetCurrentClimateProfileTask();
		task.execute(roomName, selectedProfile);
	}
}
