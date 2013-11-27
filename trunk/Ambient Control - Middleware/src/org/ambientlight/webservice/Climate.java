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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.climate.ClimateManager;
import org.ambientlight.ws.container.TemperaturMode;


/**
 * @author Florian Bornkessel
 * 
 */
@Path("/climate")
public class Climate {

	@POST
	@Path("/mode")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setTemperatur(TemperaturMode mode) {
		ClimateManager manager = (AmbientControlMW.getRoom().climateManager);

		manager.setMode(mode.temp, mode.mode, mode.until);

		return Response.status(200).build();
	}


	@GET
	@Path("/pairing")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startPairing() {
		ClimateManager manager = (AmbientControlMW.getRoom().climateManager);

		manager.startPairingMode();

		return Response.status(200).build();
	}


	@GET
	@Path("/unregister/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response unregister(@PathParam("id") int adress) {
		ClimateManager manager = (AmbientControlMW.getRoom().climateManager);

		try {
			manager.setFactoryResetDevice(adress);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}

		return Response.status(200).build();
	}


	@POST
	@Path("/currentWeekProfile")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setCurrentWeekProfile(String profile) {
		ClimateManager manager = (AmbientControlMW.getRoom().climateManager);

		try {
			manager.setCurrentProfile(profile);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}

		return Response.status(200).build();
	}

}
