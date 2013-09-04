/*  Copyright 2013 Florian Bornkessel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.ambientlight.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.entities.Node;
import org.ambientlight.process.entities.Process;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.process.handler.actor.ConfigurationChangeHandler;
import org.ambientlight.process.handler.actor.ConfigurationChangeHandlerConfiguration;
import org.ambientlight.process.handler.actor.PowerStateHandler;
import org.ambientlight.process.handler.actor.PowerstateHandlerConfiguration;
import org.ambientlight.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.process.handler.event.EventGeneratorSensorAdapterConfiguration;
import org.ambientlight.process.handler.event.EventGeneratorSensorAdapterHandler;
import org.ambientlight.process.handler.event.EventToBooleanHandler;
import org.ambientlight.process.handler.event.EventToBooleanHandlerConfiguration;
import org.ambientlight.process.handler.event.FireEventHandler;
import org.ambientlight.process.handler.event.FireEventHandlerConfiguration;
import org.ambientlight.process.handler.expression.DecisionHandlerConfiguration;
import org.ambientlight.process.handler.expression.DecissionActionHandler;
import org.ambientlight.process.handler.expression.ExpressionActionHandler;
import org.ambientlight.process.handler.expression.ExpressionHandlerConfiguration;
import org.ambientlight.room.Room;
import org.ambientlight.room.RoomConfigurationFactory;


/**
 * @author Florian Bornkessel
 * 
 */
public class ProcessFactory {

	private final Room room;


	public ProcessFactory(Room room) {
		super();
		this.room = room;
	}


	public void initProcesses() {
		List<Process> processes = new ArrayList<Process>();
		for (EventProcessConfiguration processConfig : room.config.processes) {
			if (processConfig.run == false) {
				System.out.println("ProcessFactory: Ommiting process: " + processConfig.id);
				continue;
			}
			System.out.println("ProcessFactory: Building process: " + processConfig.id);
			processes.add(createProcess(processConfig));
			System.out.println("ProcessFactory: Built and setup process successfully: " + processConfig.id);
		}
		this.room.processes = processes;
	}


	public synchronized void startProcess(String processId) throws IOException {
		ProcessConfiguration processConfig = null;
		for (ProcessConfiguration currentProcess : room.config.processes) {
			if (currentProcess.id.equals(processId)) {
				processConfig = currentProcess;
				break;
			}
		}

		if (processConfig == null)
			return;

		// return if process is already running
		for (Process currentProcess : room.processes) {
			if (currentProcess.config.id.equals(processConfig.id)) {
				System.out.println("ProcessFactory: process already running: " + processConfig.id);
				return;
			}
		}

		// todo this will crash in future if there are more process types
		Process result = createProcess((EventProcessConfiguration) processConfig);
		room.processes.add(result);
		result.eventManager = room.eventManager;
		result.start();

		// persist model
		processConfig.run = true;
		RoomConfigurationFactory.saveRoomConfiguration(room.config,
				AmbientControlMW.getRoomConfigFileName());
		System.out.println("ProcessFactory: started process successfully: " + processConfig.id);
	}


	public synchronized void stopProcess(String processId) throws IOException {
		ProcessConfiguration processConfig = null;
		for (ProcessConfiguration currentProcess : room.config.processes) {
			if (currentProcess.id.equals(processId)) {
				processConfig = currentProcess;
				break;
			}
		}

		if (processConfig == null)
			return;

		// return if process is already stopped
		Process runningProcess = null;
		for (Process currentProcess : room.processes) {
			if (currentProcess.config.id.equals(processConfig.id)) {
				runningProcess = currentProcess;
				break;
			}
		}
		if (runningProcess == null) {
			System.out.println("ProcessFactory: process already stopped: " + processConfig.id);
			return;
		}


		runningProcess.suspend();
		room.processes.remove(runningProcess);

		// persist model
		processConfig.run = false;
		RoomConfigurationFactory.saveRoomConfiguration(room.config,
				AmbientControlMW.getRoomConfigFileName());
		System.out.println("ProcessFactory: stopped process successfully: " + processConfig.id);
	}


	/**
	 * @param processConfig
	 * @return
	 */
	private Process createProcess(EventProcessConfiguration processConfig) {
		Process result = new Process();
		result.config = processConfig;
		createNodes(result, 0);
		result.eventManager = room.eventManager;
		result.start();

		return result;
	}


	/**
	 * @param processConfig
	 * @param process
	 * @param i
	 */
	private void createNodes(Process process, int i) {
		System.out.println("ProcessFactory: creating Node with id: " + i + " for process: " + process.config.id);

		NodeConfiguration nodeConfig = process.config.nodes.get(i);
		Node node = new Node();
		node.config = nodeConfig;

		AbstractActionHandler handler = null;
		if (nodeConfig.actionHandler instanceof ConfigurationChangeHandlerConfiguration) {
			handler = new ConfigurationChangeHandler();
		} else if (nodeConfig.actionHandler instanceof PowerstateHandlerConfiguration) {
			handler = new PowerStateHandler();
		} else if (nodeConfig.actionHandler instanceof SimplePowerStateHandlerConfiguration) {
			handler = new PowerStateHandler();
		} else if (nodeConfig.actionHandler instanceof DecisionHandlerConfiguration) {
			handler = new DecissionActionHandler();
			createNodes(process, nodeConfig.nextNodeIds.get(1));
		} else if (nodeConfig.actionHandler instanceof ExpressionHandlerConfiguration) {
			handler = new ExpressionActionHandler();
		} else if (nodeConfig.actionHandler instanceof EventToBooleanHandlerConfiguration) {
			handler = new EventToBooleanHandler();
		} else if (nodeConfig.actionHandler instanceof FireEventHandlerConfiguration) {
			handler = new FireEventHandler();
		} else if (nodeConfig.actionHandler instanceof EventGeneratorSensorAdapterConfiguration) {
			handler = new EventGeneratorSensorAdapterHandler();
		}
		System.out.println("ProcessFactory: actionhandler for node id: " + i + " is a: " + handler.getClass().getSimpleName());
		handler.config = nodeConfig.actionHandler;
		handler.nodeIds = nodeConfig.nextNodeIds;
		node.handler = handler;

		process.nodes.put(node.config.id, node);

		if (nodeConfig.nextNodeIds.isEmpty() == false) {
			createNodes(process, nodeConfig.nextNodeIds.get(0));
		}
	}
}
