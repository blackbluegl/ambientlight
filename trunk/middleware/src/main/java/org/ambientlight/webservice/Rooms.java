package org.ambientlight.webservice;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ambientlight.AmbientControl;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.ws.Room;


@Path("/rooms")
public class Rooms {

	@GET
	@Path("/version")
	@Produces(MediaType.TEXT_PLAIN)
	public String getInfo() {
		return "Version:0.16.0";
	}


	@GET
	@Path("/names")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<String> getRoomNames() {
		return AmbientControl.getRoomNames();
	}


	@GET
	@Path("/{roomName}/config")
	@Produces(MediaType.APPLICATION_JSON)
	public Room getRoomConfiguration(@PathParam("roomName") String roomName) {

		Room room = new Room();

		room.roomName = AmbientControl.getRoom(roomName).config.roomName;

		room.alarmManager = AmbientControl.getRoom(roomName).config.alarmManager;
		room.climateManager = AmbientControl.getRoom(roomName).config.climateManager;
		room.processManager = AmbientControl.getRoom(roomName).config.processManager;
		room.remoteSwitchesManager = AmbientControl.getRoom(roomName).config.remoteSwitchesManager;
		room.sceneriesManager = AmbientControl.getRoom(roomName).config.sceneriesManager;
		room.switchesManager = AmbientControl.getRoom(roomName).config.switchesManager;
		room.lightObjectManager = AmbientControl.getRoom(roomName).config.lightObjectManager;

		for (EntityId currentId : AmbientControl.getRoom(roomName).featureFacade.getSwitchableIds()) {
			Switchable currentSwitch = AmbientControl.getRoom(roomName).featureFacade.getSwitchable(currentId);
			room.switchables.add(currentSwitch);
		}
		return room;
	}
}