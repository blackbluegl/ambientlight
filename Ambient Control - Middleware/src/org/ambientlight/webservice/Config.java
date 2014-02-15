package org.ambientlight.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.actor.types.SwitchableId;
import org.ambientlight.ws.Room;


@Path("/config")
public class Config {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getInfo() {
		return "Version:0.16.0";
	}


	@GET
	@Path("/room")
	@Produces(MediaType.APPLICATION_JSON)
	public Room getRoomConfiguration() {

		Room room = new Room();
		room.roomConfig = AmbientControlMW.getRoom().config;

		for (SwitchableId currentId : AmbientControlMW.getRoom().featureFacade.getSwitchableIds()) {
			Switchable currentSwitch = AmbientControlMW.getRoom().featureFacade.getSwitchable(currentId.type, currentId.id);
			room.switchables.add(currentSwitch);
		}

		return room;
	}
}