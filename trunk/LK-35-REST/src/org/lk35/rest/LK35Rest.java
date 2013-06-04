package org.lk35.rest;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lk35.LK35ColorHandler;
import org.lk35.LK35ColorHandlerImpl;
import org.lk35.LK35StandaloneHTTP;


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

/**
 * @author Florian Bornkessel
 * 
 */
@Path("/LK35ColorHandler")
public class LK35Rest {

	LK35ColorHandler api;


	public LK35Rest() {
		this.api = new LK35ColorHandlerImpl(LK35StandaloneHTTP.sessionOutputStream);
	}


	@GET
	@Path("/color/rgb")
	@Produces(MediaType.TEXT_HTML)
	public Response setRGB(@QueryParam(value = "zone") List<Integer> zones, @QueryParam(value = "r") int r,
			@QueryParam(value = "g") int g, @QueryParam(value = "b") int b) throws IOException, InterruptedException {
		api.setRGB(zones, r, g, b);
		return Response.status(200).build();
	}


	public void setHSV(@QueryParam(value = "zone") List<Integer> zones, int h, int s, int v) throws IOException,
	InterruptedException {
		api.setHSV(zones, h, s, v);
	}


	@GET
	@Path("/color/rgbw")
	@Produces(MediaType.TEXT_HTML)
	public Response setRGBWithWhiteChannel(@QueryParam(value = "zone") List<Integer> zones, @QueryParam(value = "r") int r,
			@QueryParam(value = "g") int g, @QueryParam(value = "b") int b,
			@QueryParam(value = "maxBrightness") boolean maxBrightness) throws IOException, InterruptedException {
		api.setRGBWithWhiteChannel(zones, r, g, b, maxBrightness);
		return Response.status(200).build();
	}


	public void setHSVwithWihiteChannel(@QueryParam(value = "zone") List<Integer> zones, int h, int s, int v,
			boolean maxBrightness) throws IOException,
	InterruptedException {
		api.setHSVwithWihiteChannel(zones, h, s, v, maxBrightness);
	}


	public void setR(@QueryParam(value = "zone") List<Integer> zones, int value) throws IOException, InterruptedException {
		api.setR(zones, value);
	}


	public void setG(@QueryParam(value = "zone") List<Integer> zones, int value) throws InterruptedException, IOException {
		api.setG(zones, value);
	}


	public void setB(@QueryParam(value = "zone") List<Integer> zones, int value) throws InterruptedException, IOException {
		api.setB(zones, value);
	}


	@GET
	@Path("/color/w")
	@Produces(MediaType.TEXT_HTML)
	public Response setW(@QueryParam(value = "zone") List<Integer> zones, @QueryParam(value = "value") int value)
			throws InterruptedException, IOException {
		api.setW(zones, value);
		return Response.status(200).build();
	}


	public void resetColor(@QueryParam(value = "zone") List<Integer> zones) throws IOException, InterruptedException {
		api.resetColor(zones);
	}


	public void togglePower(boolean powerState) throws IOException {
		api.togglePower(powerState);
	}


	public void togglePower(@QueryParam(value = "zone") List<Integer> zones, boolean powerState) {
		api.togglePower(zones, powerState);
	}


	public void setBrightness(@QueryParam(value = "zone") List<Integer> zones, int value) throws InterruptedException,
	IOException {
		api.setBrightness(zones, value);
	}


	public void toggleColorFader(@QueryParam(value = "zone") List<Integer> zones) throws IOException, InterruptedException {
		api.toggleColorFader(zones);
	}


	public void speedUpColorFader(@QueryParam(value = "zone") List<Integer> zones) throws IOException, InterruptedException {
		api.speedUpColorFader(zones);
	}


	public void speedDownColorFader(@QueryParam(value = "zone") List<Integer> zones) throws IOException, InterruptedException {
		api.speedDownColorFader(zones);
	}

}
