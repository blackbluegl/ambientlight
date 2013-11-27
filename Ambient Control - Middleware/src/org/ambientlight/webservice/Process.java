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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.process.EventProcessConfiguration;
import org.ambientlight.config.process.NodeConfiguration;
import org.ambientlight.config.process.ProcessConfiguration;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.config.process.handler.expression.DecisionHandlerConfiguration;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.ws.process.validation.HandlerDataTypeValidation;
import org.ambientlight.ws.process.validation.ValidationResult;


/**
 * @author Florian Bornkessel
 * 
 */
// TODO think about where to handle the file saving operations. in ws or deeper
// within the actions? where is the api facade?
@Path("/process")
public class Process {

	@POST
	@Path("/validation/processes")
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

		RoomConfigurationFactory.beginTransaction();
		Integer positionToReplace = null;
		for (ProcessConfiguration currentProcess : AmbientControlMW.getRoom().config.processes) {
			if (currentProcess.id.equals(process.id)) {
				positionToReplace = AmbientControlMW.getRoom().config.processes.indexOf(currentProcess);
				break;
			}
		}

		if (positionToReplace != null) {
			AmbientControlMW.getRoom().config.processes.remove(positionToReplace.intValue());
			AmbientControlMW.getRoom().config.processes.add(positionToReplace, process);
		} else {
			AmbientControlMW.getRoom().config.processes.add(process);
		}

		RoomConfigurationFactory.commitTransaction();

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();

		return result;
	}


	@DELETE
	@Path("/processes/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object deleteProcess(@PathParam(value = "id") String id) {
		RoomConfigurationFactory.beginTransaction();
		for (ProcessConfiguration currentProcess : AmbientControlMW.getRoom().config.processes) {
			if (currentProcess.id.equals(id)) {
				stopProcess(id);
				AmbientControlMW.getRoom().config.processes.remove(currentProcess);
				break;
			}
		}

		RoomConfigurationFactory.commitTransaction();

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();

		return Response.status(200).build();
	}


	@GET
	@Path("/start/processes/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object startProcess(@PathParam(value = "id") String id) {
		System.out.println("ProcessWS: starting Process " + id);

		try {
			AmbientControlMW.getProcessFactory().startProcess(id);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();

		return Response.status(200).build();
	}


	@GET
	@Path("/stop/processes/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object stopProcess(@PathParam(value = "id") String id) {
		System.out.println("ProcessWS: stopping Process " + id);

		try {
			AmbientControlMW.getProcessFactory().stopProcess(id);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();

		return Response.status(200).build();
	}
}
