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
import org.ambientlight.process.handler.AbstractActionHandler;
import org.ambientlight.process.handler.actor.ConfigurationChangeHandler;
import org.ambientlight.process.handler.actor.ConfigurationChangeHandlerConfiguration;
import org.ambientlight.process.handler.actor.PowerStateHandler;
import org.ambientlight.process.handler.actor.PowerstateHandlerConfiguration;
import org.ambientlight.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.process.handler.process.EventMappingHandler;
import org.ambientlight.process.handler.process.ForkActionHandler;
import org.ambientlight.process.handler.process.ForkHandlerConfiguration;
import org.ambientlight.room.RoomConfiguration;


/**
 * @author Florian Bornkessel
 * 
 */
public class ProcessFactory {

	public List<Process> initProcesses(RoomConfiguration roomConfig) {
		List<Process> processes = new ArrayList<Process>();
		for (ProcessConfiguration processConfig : roomConfig.processes) {
			Process process = createProcess(processConfig);
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
		result.token = new Token();
		result.handler = new EventMappingHandler();
		result.handler.config.nextNodeId = 0;
		createNodes(processConfig, result, 0);
		return result;
	}


	/**
	 * @param processConfig
	 * @param result
	 * @param i
	 */
	private void createNodes(ProcessConfiguration processConfig, Process result, int i) {
		NodeConfiguration nodeConfig = processConfig.nodes.get(i);
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

		if (nodeConfig.actionHandler instanceof ForkHandlerConfiguration) {
			handler = new ForkActionHandler();
			if (nodeConfig.actionHandler.nextNodeId != null) {
				{
					createNodes(processConfig, result,
							((ForkHandlerConfiguration) nodeConfig.actionHandler).nextNodeIdAlternative);
				}
			}

			handler.config = nodeConfig.actionHandler;
			node.handler = handler;

			result.nodes.put(node.config.id, node);

			if (nodeConfig.actionHandler.nextNodeId == null)
				return;
			else {
				createNodes(processConfig, result, nodeConfig.actionHandler.nextNodeId);
			}
		}
	}
}
