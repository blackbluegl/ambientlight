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
 * register clients that listen to status changes in the room. see callback package for details of the comunication.
 * 
 * @author Florian Bornkessel
 */
@Path("/callback")
public class Callback {

	@PUT
	@Path("/{roomName}/client")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response register(@PathParam("roomName") String roomName, String ipAndPort) {
		AmbientControl.getRoom(roomName).callBackManager.registerClient(ipAndPort);
		return Response.status(200).build();
	}


	@DELETE
	@Path("/{roomName}/client/{ipAndPort}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response unregister(@PathParam("roomName") String roomName, @PathParam("ipAndPort") String ipAndPort) {
		AmbientControl.getRoom(roomName).callBackManager.unregisterClient(ipAndPort);
		return Response.status(200).build();
	}
}
