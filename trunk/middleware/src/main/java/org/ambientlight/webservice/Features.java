package org.ambientlight.webservice;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControl;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.room.entities.features.EntityId;


@Path("/features")
public class Features {

	@GET
	@Path("/{roomName}/switchables")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<EntityId> getSwitchables(@PathParam("roomName") String roomName) {
		return AmbientControl.getRoom(roomName).featureFacade.getSwitchableIds();
	}


	@PUT
	@Path("/{roomName}/switchables/{type}/{id}/state")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object setPowerStateForItem(@PathParam("roomName") String roomName, @PathParam("domain") String domain,
			@PathParam("id") String itemName, Boolean powerState) {

		try {

			AmbientControl.getRoom(roomName).featureFacade.setSwitcheablePowerState(typeEnume, itemName, powerState, true);
			return Response.status(200).build();

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}


	@PUT
	@Path("/{roomName}/renderables/{id}/config")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object setRenderConfigurationForItem(@PathParam("roomName") String roomName, @PathParam("id") String itemName,
			RenderingProgramConfiguration config) {

		try {
			AmbientControl.getRoom(roomName).featureFacade.setRenderingConfiguration(config, itemName);
			return Response.status(200).build();

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}


	@GET
	@Path("/{roomName}/switchables/{type}/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object getSwitchable(@PathParam("roomName") String roomName, @PathParam("type") String type,
			@PathParam("id") String itemName) {

		try {
			SwitchType typeEnume = SwitchType.valueOf(type);
			return AmbientControl.getRoom(roomName).featureFacade.getSwitchable(typeEnume, itemName);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}
}