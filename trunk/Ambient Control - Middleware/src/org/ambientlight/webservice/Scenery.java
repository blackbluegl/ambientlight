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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControlMW;


/**
 * @author Florian Bornkessel
 * 
 */

@Path("/sceneries")
public class Scenery {

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object createScenery(String scenery) {
		try {
			AmbientControlMW.getRoom().sceneryManager.createScenery(scenery);
			return Response.status(200).build();
		} catch (Exception e) {
			return Response.status(500).build();
		}
	}


	@POST
	@Path("/current")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object setCurrentScenery(String scenery) {
		try {
			AmbientControlMW.getRoom().sceneryManager.setCurrentScenery(scenery);
			return Response.status(200).build();
		} catch (Exception e) {
			return Response.status(500).build();
		}
	}


	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object deleteScenery(@PathParam(value = "id") String id) {
		try {
			AmbientControlMW.getRoom().sceneryManager.deleteScenery(id);
			return Response.status(200).build();
		} catch (Exception e) {
			return Response.status(500).build();
		}
	}
}
