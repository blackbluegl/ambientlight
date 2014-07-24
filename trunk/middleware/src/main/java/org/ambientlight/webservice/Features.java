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
import org.ambientlight.config.room.entities.climate.TemperaturMode;
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


	@GET
	@Path("/{roomName}/switchables/{domain}/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object getSwitchable(@PathParam("roomName") String roomName, @PathParam("domain") String domain,
			@PathParam("id") String itemName) {

		try {
			return AmbientControl.getRoom(roomName).featureFacade.getSwitchable(new EntityId(domain, itemName));
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}


	@PUT
	@Path("/{roomName}/switchables/{domain}/{id}/state")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object setPowerStateForItem(@PathParam("roomName") String roomName, @PathParam("domain") String domain,
			@PathParam("id") String itemName, Boolean powerState) {

		try {

			AmbientControl.getRoom(roomName).featureFacade.setSwitcheablePowerState(new EntityId(domain, itemName), powerState,
					true);
			return Response.status(200).build();

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}


	@GET
	@Path("/{roomName}/renderables/{domain}/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getRenderable(@PathParam("roomName") String roomName, @PathParam("domain") String domain,
			@PathParam("id") String itemName) {

		try {
			return AmbientControl.getRoom(roomName).lightObjectManager.getRenderable(new EntityId(domain, itemName));
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}


	@PUT
	@Path("/{roomName}/renderables/{domain}/{id}/config")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object setRenderConfigurationForItem(@PathParam("roomName") String roomName, @PathParam("domain") String domain,
			@PathParam("id") String itemName, RenderingProgramConfiguration config) {

		try {
			AmbientControl.getRoom(roomName).featureFacade.setRenderingConfiguration(config, new EntityId(domain, itemName));
			return Response.status(200).build();

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}


	@PUT
	@Path("/{roomName}/climate/mode/boost")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setBoostMode(@PathParam("roomName") String roomName, boolean enable) {

		AmbientControl.getRoom(roomName).featureFacade.setClimateBoostMode(enable);
		return Response.status(200).build();
	}


	@PUT
	@Path("/{roomName}/climate/mode")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setMode(@PathParam("roomName") String roomName, TemperaturMode mode) {

		AmbientControl.getRoom(roomName).featureFacade.setClimate(mode);
		return Response.status(200).build();
	}

}