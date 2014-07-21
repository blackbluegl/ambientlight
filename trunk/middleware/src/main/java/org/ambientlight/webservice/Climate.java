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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControl;
import org.ambientlight.room.entities.climate.ClimateManager;


/**
 * @author Florian Bornkessel
 * 
 */
@Path("/config/climate")
public class Climate {


	@GET
	@Path("/{roomName}/pairing")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startPairing(@PathParam("roomName") String roomName) {
		ClimateManager manager = (AmbientControl.getRoom(roomName).climateManager);

		manager.setPairingMode();

		return Response.status(200).build();
	}


	@GET
	@Path("/{roomName}/unregister/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response unregister(@PathParam("roomName") String roomName, @PathParam("id") int adress) {
		ClimateManager manager = AmbientControl.getRoom(roomName).climateManager;

		try {
			manager.setFactoryResetDevice(adress);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}

		return Response.status(200).build();
	}


	@GET
	@Path("/{roomName}/currentWeekProfile/{currentProfile}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setCurrentWeekProfile(@PathParam("roomName") String roomName, @PathParam("currentProfile") String profile) {
		ClimateManager manager = AmbientControl.getRoom(roomName).climateManager;

		try {
			manager.setCurrentProfile(profile);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}

		return Response.status(200).build();
	}

}
