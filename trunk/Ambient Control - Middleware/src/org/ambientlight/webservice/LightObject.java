package org.ambientlight.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;


@Path("/lightObject")
public class LightObject {

	@PUT
	@Path("/{itemName}/program")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public synchronized Response createOrUpdateLightObjectConfiguration(@PathParam("itemName") String itemName,
			RenderingProgramConfiguration itemConfiguration) {

		try {
			AmbientControlMW.getRoom().lightObjectManager.setRenderingConfiguration(itemConfiguration, itemName);
			return Response.status(500).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(200).build();
		}
	}
}