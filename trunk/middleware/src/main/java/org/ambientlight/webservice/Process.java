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
import org.ambientlight.config.process.ProcessConfiguration;


/**
 * @author Florian Bornkessel
 * 
 */

@Path("/process")
public class Process {

	@POST
	@Path("/validation")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object validateProcess(ProcessConfiguration process) {
		try {
			return AmbientControlMW.getRoom().processManager.validateProcess(process);

		} catch (Exception e) {
			return Response.status(500).build();
		}
	}


	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object createOrUpdateProcess(EventProcessConfiguration process) {
		try {
			return AmbientControlMW.getRoom().processManager.createOrUpdateProcess(process);

		} catch (Exception e) {
			return Response.status(500).build();
		}
	}


	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object deleteProcess(@PathParam(value = "id") String id) {
		try {
			AmbientControlMW.getRoom().processManager.deleteProcess(id);
			return Response.status(200).build();
		} catch (Exception e) {
			return Response.status(500).build();
		}
	}


	@GET
	@Path("/start/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object startProcess(@PathParam(value = "id") String id) {
		System.out.println("ProcessWS: starting Process " + id);

		try {
			AmbientControlMW.getRoom().processManager.startProcess(id);
			return Response.status(200).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}


	@GET
	@Path("/stop/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object stopProcess(@PathParam(value = "id") String id) {
		System.out.println("ProcessWS: stopping Process " + id);

		try {
			AmbientControlMW.getRoom().processManager.stopProcess(id);

			return Response.status(200).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}

	}
}
