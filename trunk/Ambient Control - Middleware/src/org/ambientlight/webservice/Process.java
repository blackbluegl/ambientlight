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

package org.ambientlight.webservice;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.NodeConfiguration;
import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.process.handler.DataTypeValidation;
import org.ambientlight.process.handler.HandlerDataTypeValidation;
import org.ambientlight.process.handler.ValidationResult;
import org.ambientlight.room.RoomConfigurationFactory;


/**
 * @author Florian Bornkessel
 * 
 */
@Path("/process")
public class Process {

	@PUT
	@Path("/processes/validation")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ValidationResult validateProcess(ProcessConfiguration process) {
		ValidationResult result = new ValidationResult();

		List<ProcessConfiguration> processes = AmbientControlMW.getRoom().config.processes;
		for (ProcessConfiguration currentProcess : processes) {
			if (currentProcess.id.equals(process.id)) {
				result.idExists = true;
				break;
			}
		}

		for (NodeConfiguration currentNode : process.nodes.values()) {

			if (currentNode.actionHandler == null) {
				result.addEmptyActionHandlerEntry(currentNode.id);
				continue;
			}

			HandlerDataTypeValidation currentNodeValidation = currentNode.actionHandler.getClass().getAnnotation(
					HandlerDataTypeValidation.class);

			for (Integer nextNodeId : currentNode.nextNodeIds) {
				NodeConfiguration nextNode = process.nodes.get(nextNodeId);
				HandlerDataTypeValidation nextNodeValidation = nextNode.actionHandler.getClass().getAnnotation(
						HandlerDataTypeValidation.class);
				boolean valide = DataTypeValidation.validate(nextNodeValidation.consumes(), currentNodeValidation.generates());
				if (!valide) {
					result.addEntry(nextNodeId, currentNode.id, currentNodeValidation.generates(), nextNodeValidation.consumes());
				}
			}
		}
		return result;
	}


	@PUT
	@Path("/processes/new")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object createProcess(ProcessConfiguration process) {
		ValidationResult result = this.validateProcess(process);
		if (result.resultIsValid() == false)
			return result;

		AmbientControlMW.getRoom().config.processes.add(process);

		try {
			RoomConfigurationFactory.saveRoomConfiguration(AmbientControlMW.getRoom().config,
					AmbientControlMW.getRoomConfigFileName());
		} catch (IOException e) {
			e.printStackTrace();

			return Response.status(500).build();
		}

		return result;
	}
}
