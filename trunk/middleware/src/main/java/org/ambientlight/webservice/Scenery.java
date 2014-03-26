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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControl;


/**
 * @author Florian Bornkessel
 * 
 */

@Path("/sceneries")
public class Scenery {

	@PUT
	@Path("/{roomName}/new")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Object createScenery(@PathParam("roomName") String roomName, String scenery) {
		try {
			AmbientControl.getRoom(roomName).sceneryManager.createScenery(scenery);
			return Response.status(200).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}


	@PUT
	@Path("/{roomName}/current")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Object setCurrentScenery(@PathParam("roomName") String roomName, String scenery) {
		try {
			AmbientControl.getRoom(roomName).sceneryManager.setCurrentScenery(scenery);
			return Response.status(200).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}


	@DELETE
	@Path("/{roomName}/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object deleteScenery(@PathParam("roomName") String roomName, @PathParam(value = "id") String id) {
		try {
			AmbientControl.getRoom(roomName).sceneryManager.deleteScenery(id);
			return Response.status(200).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}
}
