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
import org.ambientlight.config.events.BroadcastEvent;
import org.ambientlight.config.events.SceneryEntryEvent;
import org.ambientlight.config.events.SwitchEvent;
import org.ambientlight.room.entities.EventGenerator;
import org.ambientlight.room.entities.SceneryEventGenerator;
import org.ambientlight.room.entities.switches.SwitchManager;


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
	public Response handleEvent(BroadcastEvent event) {
		EventGenerator eventGen = (AmbientControlMW.getRoom().eventGenerators.get(event.sourceId));
		if (event instanceof SceneryEntryEvent) {
			((SceneryEventGenerator) eventGen).sceneryEntryEventOccured((SceneryEntryEvent) event);
		}

		if (event instanceof SwitchEvent) {
			((SwitchManager) eventGen).switchEventOccured((SwitchEvent) event);
		}

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
		return Response.status(200).build();
	}
}
