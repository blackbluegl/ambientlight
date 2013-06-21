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
import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.process.events.EventManager;
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.process.handler.actor.ConfigurationChangeHandler;
import org.ambientlight.process.handler.actor.ConfigurationChangeHandlerConfiguration;
import org.ambientlight.process.handler.actor.PowerStateHandler;
import org.ambientlight.process.handler.actor.PowerstateHandlerConfiguration;
import org.ambientlight.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.process.handler.event.EventToBooleanHandler;
import org.ambientlight.process.handler.event.EventToBooleanHandlerConfiguration;
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
		for (ProcessConfiguration processConfig : roomConfig.processes) {
			System.out.println("ProcessFactory: Building process: " + processConfig.id);
			Process process = createProcess(processConfig);
			process.eventManager = eventManager;
			processes.add(process);
			process.start();
		}
		return processes;
	}


	/**
	 * @param processConfig
	 * @return
	 */
	private Process createProcess(ProcessConfiguration processConfig) {
		Process result = new Process();
		result.config = processConfig;
		result.token = new Token();
		createNodes(result, 0);
		return result;
	}


	/**
	 * @param processConfig
	 * @param process
	 * @param i
	 */
	private void createNodes(Process process, int i) {
		NodeConfiguration nodeConfig = process.config.nodes.get(i);
		Node node = new Node();
		node.config = nodeConfig;

		AbstractActionHandler handler = null;
		if (nodeConfig.actionHandler instanceof ConfigurationChangeHandlerConfiguration) {
			handler = new ConfigurationChangeHandler();
		}

		if (nodeConfig.actionHandler instanceof PowerstateHandlerConfiguration) {
			handler = new PowerStateHandler();
		}

		if (nodeConfig.actionHandler instanceof SimplePowerStateHandlerConfiguration) {
			handler = new PowerStateHandler();
		}

		if (nodeConfig.actionHandler instanceof DecisionHandlerConfiguration) {
			handler = new DecissionActionHandler();
			createNodes(process, ((DecisionHandlerConfiguration) nodeConfig.actionHandler).nextAlternativeNodeId);
		}

		if (nodeConfig.actionHandler instanceof ExpressionHandlerConfiguration) {
			handler = new ExpressionActionHandler();
		}

		if (nodeConfig.actionHandler instanceof EventToBooleanHandlerConfiguration) {
			handler = new EventToBooleanHandler();
		}

		handler.config = nodeConfig.actionHandler;
		node.handler = handler;

		process.nodes.put(node.config.id, node);

		if (nodeConfig.actionHandler.nextNodeId != null) {
			createNodes(process, nodeConfig.actionHandler.nextNodeId);
		}
	}
}
