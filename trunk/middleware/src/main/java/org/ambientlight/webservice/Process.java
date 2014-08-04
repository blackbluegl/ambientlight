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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControl;
import org.ambientlight.config.process.EventProcessConfiguration;
import org.ambientlight.config.process.ProcessConfiguration;


/**
 * @author Florian Bornkessel
 * 
 */

@Path("/process")
public class Process {

	@POST
	@Path("/{roomName}/validation")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object validateProcess(@PathParam("roomName") String roomName, ProcessConfiguration process) {
		try {
			return AmbientControl.getRoom(roomName).processManager.validateProcess(process);

		} catch (Exception e) {
			System.out.println("Process - validateProcess(): could not validate processConfig!");
			e.printStackTrace();
			return Response.status(500).build();
		}
	}


	@POST
	@Path("/{roomName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object createOrUpdateProcess(@PathParam("roomName") String roomName, EventProcessConfiguration process) {
		try {
			return AmbientControl.getRoom(roomName).processManager.createOrUpdateProcess(process);

		} catch (Exception e) {
			return Response.status(500).build();
		}
	}


	@DELETE
	@Path("/{roomName}/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object deleteProcess(@PathParam("roomName") String roomName, @PathParam(value = "id") String id) {
		try {
			AmbientControl.getRoom(roomName).processManager.deleteProcess(id);
			return Response.status(200).build();
		} catch (Exception e) {
			return Response.status(500).build();
		}
	}


	@PUT
	@Path("/{roomName}/{id}/state")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object startStopProcess(@PathParam("roomName") String roomName, @PathParam(value = "id") String id, Boolean state) {

		try {
			if (state) {
				System.out.println("ProcessWS: starting Process " + id);
				AmbientControl.getRoom(roomName).processManager.startProcess(id);
			} else {
				System.out.println("ProcessWS: stopping Process " + id);
				AmbientControl.getRoom(roomName).processManager.stopProcess(id);
			}
			return Response.status(200).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}
}
