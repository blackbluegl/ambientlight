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

package org.ambientlight.process.entities;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.process.NodeConfiguration;
import org.ambientlight.process.EventProcessConfiguration;
import org.ambientlight.process.eventmanager.EventManager;
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
import org.ambientlight.room.RoomConfiguration;


/**
 * @author Florian Bornkessel
 * 
 */
public class ProcessFactory {

	public List<Process> initProcesses(RoomConfiguration roomConfig, EventManager eventManager) {
		List<Process> processes = new ArrayList<Process>();
		for (EventProcessConfiguration processConfig : roomConfig.processes) {
			System.out.println("ProcessFactory: Building process: " + processConfig.id);
			Process process = createProcess(processConfig);
			process.eventManager = eventManager;
			processes.add(process);
			process.start();
			System.out.println("ProcessFactory: Built and setup process successfully: " + processConfig.id);
		}
		return processes;
	}


	/**
	 * @param processConfig
	 * @return
	 */
	private Process createProcess(EventProcessConfiguration processConfig) {
		Process result = new Process();
		result.config = processConfig;
		createNodes(result, 0);
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
