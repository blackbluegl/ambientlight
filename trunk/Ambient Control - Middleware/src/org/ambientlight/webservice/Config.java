package org.ambientlight.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.room.RoomConfiguration;


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
	public RoomConfiguration getRoomConfiguration() {
		return AmbientControlMW.getRoom().config;
	}
}