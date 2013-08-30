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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.EventProcessConfiguration;
import org.ambientlight.process.NodeConfiguration;
import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.process.handler.DataTypeValidation;
import org.ambientlight.process.handler.expression.DecisionHandlerConfiguration;
import org.ambientlight.process.validation.HandlerDataTypeValidation;
import org.ambientlight.process.validation.ValidationResult;
import org.ambientlight.room.RoomConfigurationFactory;


/**
 * @author Florian Bornkessel
 * 
 */
@Path("/process")
public class Process {

	@POST
	@Path("/processes/validation")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
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
				boolean valide = DataTypeValidation.validate(nextNodeValidation.consumes(), currentNodeValidation.generates());
				if (!valide) {
					result.addEntry(nextNodeId, currentNode.id, currentNodeValidation.generates(), nextNodeValidation.consumes());
				}
			}
		}
		return result;
	}


	@POST
	@Path("/processes")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object createOrUpdateProcess(EventProcessConfiguration process) {
		ValidationResult result = this.validateProcess(process);
		if (result.resultIsValid() == false)
			return result;
		Integer positionToReplace = null;
		for (ProcessConfiguration currentProcess : AmbientControlMW.getRoom().config.processes) {
			if (currentProcess.id.equals(process.id)) {
				positionToReplace = AmbientControlMW.getRoom().config.processes.indexOf(currentProcess);
				break;
			}
		}

		if (positionToReplace != null) {
			AmbientControlMW.getRoom().config.processes.remove(positionToReplace);
			AmbientControlMW.getRoom().config.processes.add(positionToReplace, process);
		} else {
			AmbientControlMW.getRoom().config.processes.add(process);
		}

		try {
			RoomConfigurationFactory.saveRoomConfiguration(AmbientControlMW.getRoom().config,
					AmbientControlMW.getRoomConfigFileName());
		} catch (IOException e) {
			e.printStackTrace();

			return Response.status(500).build();
		}

		return result;
	}


	@DELETE
	@Path("/processes/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object createOrUpdateProcess(@PathParam(value = "id") String id) {
		for (ProcessConfiguration currentProcess : AmbientControlMW.getRoom().config.processes) {
			if (currentProcess.id.equals(id)) {
				AmbientControlMW.getRoom().config.processes.remove(currentProcess);
				break;
			}
		}

		try {
			RoomConfigurationFactory.saveRoomConfiguration(AmbientControlMW.getRoom().config,
					AmbientControlMW.getRoomConfigFileName());
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(500).build();
		}

		return Response.status(200).build();
	}
}
