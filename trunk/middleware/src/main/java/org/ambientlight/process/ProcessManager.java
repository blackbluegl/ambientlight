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

import org.ambientlight.Manager;
import org.ambientlight.Persistence;
import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.process.EventProcessConfiguration;
import org.ambientlight.config.process.NodeConfiguration;
import org.ambientlight.config.process.ProcessConfiguration;
import org.ambientlight.config.process.ProcessManagerConfiguration;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.config.process.handler.actor.RenderingProgrammChangeHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.SceneryHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.SwitchableHandlerConfiguration;
import org.ambientlight.config.process.handler.event.SensorToTokenConfiguration;
import org.ambientlight.config.process.handler.expression.DecisionHandlerConfiguration;
import org.ambientlight.config.process.handler.expression.ExpressionHandlerConfiguration;
import org.ambientlight.events.EventManager;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.process.handler.actor.RenderingProgrammChangeHandler;
import org.ambientlight.process.handler.actor.SceneryHandler;
import org.ambientlight.process.handler.actor.SimplePowerStateHandler;
import org.ambientlight.process.handler.actor.SwitchableHandler;
import org.ambientlight.process.handler.event.SensorToTokenHandler;
import org.ambientlight.process.handler.expression.DecissionActionHandler;
import org.ambientlight.process.handler.expression.ExpressionActionHandler;
import org.ambientlight.room.entities.FeatureFacade;
import org.ambientlight.ws.process.validation.HandlerDataTypeValidation;
import org.ambientlight.ws.process.validation.ValidationResult;


/**
 * @author Florian Bornkessel
 * 
 */
public class ProcessManager extends Manager {

	private FeatureFacade featureFacade;

	private ProcessManagerConfiguration config;

	private EventManager eventManager;

	private CallBackManager callback;

	private Map<String, Process> processes = new HashMap<String, Process>();


	public ProcessManager(ProcessManagerConfiguration config, EventManager eventManager, CallBackManager callback,
			FeatureFacade featureFacade, Persistence persistence) {
		this.config = config;
		this.eventManager = eventManager;
		this.persistence = persistence;
		this.callback = callback;
		this.featureFacade = featureFacade;

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

		persistence.beginTransaction();

		Process result = createProcess((EventProcessConfiguration) processConfig);
		result.start();

		processes.put(processConfig.id, result);

		processConfig.run = true;

		persistence.commitTransaction();

		callback.roomConfigurationChanged();

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
		processes.remove(processId);

		persistence.beginTransaction();
		processConfig.run = false;

		persistence.commitTransaction();

		callback.roomConfigurationChanged();

		System.out.println("ProcessFactory: stopped process successfully: " + processConfig.id);
	}


	/**
	 * @param processConfig
	 * @return
	 */
	private Process createProcess(EventProcessConfiguration processConfig) {
		Process result = new Process();
		result.callback = this.callback;
		result.persistence = this.persistence;
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
		// actores
		if (nodeConfig.actionHandler instanceof RenderingProgrammChangeHandlerConfiguration) {
			handler = new RenderingProgrammChangeHandler();
		} else if (nodeConfig.actionHandler instanceof SceneryHandlerConfiguration) {
			handler = new SceneryHandler();
		} else if (nodeConfig.actionHandler instanceof SwitchableHandlerConfiguration) {
			handler = new SwitchableHandler();
		} else if (nodeConfig.actionHandler instanceof SimplePowerStateHandlerConfiguration) {
			handler = new SimplePowerStateHandler();
		}
		// expressions
		else if (nodeConfig.actionHandler instanceof DecisionHandlerConfiguration) {
			handler = new DecissionActionHandler();
			createNodes(process, nodeConfig.nextNodeIds.get(1));
		} else if (nodeConfig.actionHandler instanceof ExpressionHandlerConfiguration) {
			handler = new ExpressionActionHandler();
		}
		// sensor
		else if (nodeConfig.actionHandler instanceof SensorToTokenConfiguration) {
			handler = new SensorToTokenHandler();
		}

		System.out.println("ProcessFactory: actionhandler for node id: " + i + " is a: " + handler.getClass().getSimpleName());
		handler.config = nodeConfig.actionHandler;
		handler.featureFacade = this.featureFacade;
		handler.eventManager = this.eventManager;
		handler.nodeIds = nodeConfig.nextNodeIds;
		node.handler = handler;

		process.nodes.put(node.config.id, node);

		if (nodeConfig.nextNodeIds.isEmpty() == false) {
			createNodes(process, nodeConfig.nextNodeIds.get(0));
		}
	}


	public ValidationResult validateProcess(ProcessConfiguration process) {
		ValidationResult result = new ValidationResult();

		for (NodeConfiguration currentNode : process.nodes.values()) {

			if (currentNode.actionHandler == null) {
				result.addEmptyActionHandlerEntry(currentNode.id);
				continue;
			}

			if (currentNode.nextNodeIds.size() > 1) {
				if (currentNode.actionHandler instanceof DecisionHandlerConfiguration == false) {
					result.addForkWithoutCorrespondingHandler(currentNode.id);
				}
			}

			HandlerDataTypeValidation currentNodeValidation = currentNode.actionHandler.getClass().getAnnotation(
					HandlerDataTypeValidation.class);

			for (Integer nextNodeId : currentNode.nextNodeIds) {
				NodeConfiguration nextNode = process.nodes.get(nextNodeId);
				if (nextNode.actionHandler == null) {
					// do not validate this connection because it will be
					// validatet at the beginning of the outer for loop
					continue;
				}
				HandlerDataTypeValidation nextNodeValidation = nextNode.actionHandler.getClass().getAnnotation(
						HandlerDataTypeValidation.class);
				if (nextNodeValidation == null) {
					System.out.println("ProcessManager - validateProcess(): node with id: " + nextNodeId
							+ " contains no dataTypeValidationInfo!");
				}
				boolean valide = DataTypeValidation.validate(nextNodeValidation.consumes(), currentNodeValidation.generates());
				if (!valide) {
					result.addEntry(nextNodeId, currentNode.id, currentNodeValidation.generates(), nextNodeValidation.consumes());
					System.out.println("ProcessManager - validateProcess():validation failed between node: " + currentNode.id
							+ "," + currentNode.actionHandler.getClass().getSimpleName() + " and node: " + nextNode.id + ","
							+ nextNode.actionHandler.getClass().getSimpleName() + "!");
				}
			}
		}
		return result;
	}


	public ValidationResult createOrUpdateProcess(EventProcessConfiguration process) {
		ValidationResult result = this.validateProcess(process);
		if (result.resultIsValid() == false)
			return result;

		persistence.beginTransaction();

		config.processes.put(process.id, process);

		persistence.commitTransaction();

		callback.roomConfigurationChanged();

		return result;
	}


	public void deleteProcess(String id) {
		persistence.beginTransaction();
		ProcessConfiguration process = config.processes.get(id);

		if (process == null)
			throw new IllegalArgumentException("ProcessId does not exist!");

		stopProcess(id);
		config.processes.remove(id);

		persistence.commitTransaction();

		callback.roomConfigurationChanged();
	}
}
