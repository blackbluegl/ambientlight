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

package org.ambientlight.scenery.ws;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.process.events.event.Event;
import org.ambientlight.process.events.event.SceneryEvent;
import org.ambientlight.process.events.event.SwitchEvent;
import org.ambientlight.process.events.generator.EventGenerator;
import org.ambientlight.process.events.generator.SceneryEventGenerator;
import org.ambientlight.process.events.generator.SwitchEventGenerator;


/**
 * @author Florian Bornkessel
 * 
 */
@Path("/eventReceiver")
public class EventReceiver {

	@PUT
	@Path("/eventReceiver/eventGenerator/{eventGenerator}/event/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response handleEvent(Event event, @PathParam("eventGenerator") String eventGeneratorName) {
		EventGenerator eventGen = (AmbientControlMW.getRoom().eventGenerators.get(eventGeneratorName));
		if (event instanceof SceneryEvent) {
			((SceneryEventGenerator) eventGen).sceneryEntryEventOccured((SceneryEvent) event);
		}

		if (event instanceof SwitchEvent) {
			((SwitchEventGenerator) eventGen).switchEventOccured((SwitchEvent) event);
		}
		return Response.status(200).build();
	}
}
