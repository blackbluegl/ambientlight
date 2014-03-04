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

import org.ambientlight.AmbientControlMW;
import org.ambientlight.room.entities.features.actor.types.SwitchType;
import org.ambientlight.room.entities.features.actor.types.SwitchableId;


@Path("/feature")
public class Features {

	@GET
	@Path("/switchables")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<SwitchableId> getSwitchables() {
		return AmbientControlMW.getRoom().featureFacade.getSwitchableIds();
	}


	@PUT
	@Path("/switchables/{type}/{id}/state")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object setPowerStateForItem(@PathParam("type") String type, @PathParam("id") String itemName, Boolean powerState) {

		try {
			SwitchType typeEnume = SwitchType.valueOf(type);

			AmbientControlMW.getRoom().featureFacade.setSwitcheablePowerState(typeEnume, itemName, powerState);
			return Response.status(200).build();

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}


	@GET
	@Path("/switchables/{type}/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object getSwitchable(@PathParam("type") String type, @PathParam("id") String itemName) {

		try {
			SwitchType typeEnume = SwitchType.valueOf(type);
			return AmbientControlMW.getRoom().featureFacade.getSwitchable(typeEnume, itemName);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}
}