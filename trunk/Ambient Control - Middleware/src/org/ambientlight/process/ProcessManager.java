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

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.process.EventProcessConfiguration;
import org.ambientlight.config.process.NodeConfiguration;
import org.ambientlight.config.process.ProcessConfiguration;
import org.ambientlight.config.process.ProcessManagerConfiguration;
import org.ambientlight.config.process.handler.actor.RenderingProgrammChangeHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.PowerstateHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.config.process.handler.event.EventGeneratorSensorAdapterConfiguration;
import org.ambientlight.config.process.handler.event.EventToBooleanHandlerConfiguration;
import org.ambientlight.config.process.handler.event.FireEventHandlerConfiguration;
import org.ambientlight.config.process.handler.expression.DecisionHandlerConfiguration;
import org.ambientlight.config.process.handler.expression.ExpressionHandlerConfiguration;
import org.ambientlight.eventmanager.EventManager;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.process.handler.actor.RenderingProgrammChangeHandler;
import org.ambientlight.process.handler.actor.PowerStateHandler;
import org.ambientlight.process.handler.event.EventGeneratorSensorAdapterHandler;
import org.ambientlight.process.handler.event.EventToBooleanHandler;
import org.ambientlight.process.handler.event.FireEventHandler;
import org.ambientlight.process.handler.expression.DecissionActionHandler;
import org.ambientlight.process.handler.expression.ExpressionActionHandler;
import org.ambientlight.room.Persistence;


/**
 * @author Florian Bornkessel
 * 
 */
public class ProcessManager {

	private ProcessManagerConfiguration config;

	private EventManager eventManager;

	private Map<String, Process> processes = new HashMap<String, Process>();


	public ProcessManager(ProcessManagerConfiguration config, EventManager eventManager) {
		this.config = config;
		this.eventManager = eventManager;

		Map<String, Process> processes = new HashMap<String, Process>();
		for (EventProcessConfiguration processConfig : config.processes.values()) {
			if (processConfig.run == false) {
				System.out.println("ProcessFactory: Ommiting process: " + processConfig.id);
				continue;
			}
			System.out.println("ProcessFactory: Building process: " + processConfig.id);
			Process result = createProcess(processConfig);
			result.start();

			processes.put(processConfig.id, result);

			System.out.println("ProcessFactory: Built and setup process successfully: " + processConfig.id);
		}

		this.processes = processes;
	}


	public void startProcess(String processId) {

		ProcessConfiguration processConfig = config.processes.get(processId);

		if (processConfig == null)
			throw new IllegalArgumentException("ProcessId does not exist!");

		// return if process is already running
		if (processes.containsKey(processConfig.id)) {
			System.out.println("ProcessFactory: process already running: " + processConfig.id);
			return;
		}

		Persistence.beginTransaction();

		Process result = createProcess((EventProcessConfiguration) processConfig);
		result.start();

		processes.put(processConfig.id, result);

		processConfig.run = true;

		Persistence.commitTransaction();

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();

		System.out.println("ProcessFactory: started process successfully: " + processConfig.id);
	}


	public void stopProcess(String processId) {

		ProcessConfiguration processConfig = config.processes.get(processId);

		if (processConfig == null)
			throw new IllegalArgumentException("ProcessId does not exist!");

		// return if process is already stopped
		Process runningProcess = processes.get(processId);
		if (runningProcess == null) {
			System.out.println("ProcessFactory: process already stopped: " + processConfig.id);
			return;
		}

		runningProcess.suspend();
		processes.remove(runningProcess);

		Persistence.beginTransaction();
		processConfig.run = false;

		Persistence.commitTransaction();

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();

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
		result.eventManager = eventManager;

		return result;
	}


	private void createNodes(Process process, int i) {
		System.out.println("ProcessFactory: creating Node with id: " + i + " for process: " + process.config.id);

		NodeConfiguration nodeConfig = process.config.nodes.get(i);
		Node node = new Node();
		node.config = nodeConfig;

		AbstractActionHandler handler = null;
		if (nodeConfig.actionHandler instanceof RenderingProgrammChangeHandlerConfiguration) {
			handler = new RenderingProgrammChangeHandler();
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
