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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.events.EventConfiguration;
import org.ambientlight.process.events.SceneryEntryEventConfiguration;
import org.ambientlight.process.events.SwitchEventConfiguration;
import org.ambientlight.room.entities.EventGenerator;
import org.ambientlight.room.entities.SceneryEventGenerator;
import org.ambientlight.room.entities.SwitchEventGenerator;


/**
 * @author Florian Bornkessel
 * 
 */
@Path("/eventReceiver")
public class EventReceiver {

	@PUT
	@Path("/event")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response handleEvent(EventConfiguration event) {
		EventGenerator eventGen = (AmbientControlMW.getRoom().eventGenerators.get(event.eventGeneratorName));
		if (event instanceof SceneryEntryEventConfiguration) {
			((SceneryEventGenerator) eventGen).sceneryEntryEventOccured((SceneryEntryEventConfiguration) event);
		}

		if (event instanceof SwitchEventConfiguration) {
			((SwitchEventGenerator) eventGen).switchEventOccured((SwitchEventConfiguration) event);
		}
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
		return Response.status(200).build();
	}
}