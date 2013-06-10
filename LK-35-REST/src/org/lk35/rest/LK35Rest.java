package org.lk35.rest;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lk35.LK35StandaloneHTTP;
import org.lk35.api.LK35ColorHandler;
import org.lk35.api.LK35ColorHandlerImpl;


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
 * This rest handler wraps all methods of the LK35ColorHandler. Have a look at
 * the api documentation for details. All parameters will be mapped by
 * Query-Parameters. Several parameters with the same name will be treated as
 * ArrayList. The basepath is:
 * <code>http://[ip]:[port]/rest/LK35ColorHandler</code>
 * <p>
 * 
 * Example:
 * <code>http://localhost:8899/rest/LK35ColorHandler/color/rgb?zones=1&zones=2&r=222&g=222&b=0</code>
 * 
 * @author Florian Bornkessel
 * 
 */
@Path("/LK35ColorHandler")
public class LK35Rest {

	LK35ColorHandler api;


	public LK35Rest() {
		this.api = new LK35ColorHandlerImpl(LK35StandaloneHTTP.sessionOutputStream);
	}


	/**
	 * Path: /color/rgb
	 * 
	 * @param zones
	 * @param r
	 * @param g
	 * @param b
	 * @return http status 200
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@GET
	@Path("/color/rgb")
	@Produces(MediaType.TEXT_HTML)
	public Response setRGB(@QueryParam(value = "zone") List<Integer> zones, @QueryParam(value = "r") int r,
			@QueryParam(value = "g") int g, @QueryParam(value = "b") int b) throws IOException, InterruptedException {
		try {
			api.setRGB(zones, r, g, b);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}


	/**
	 * Path: /color/hsv
	 * 
	 * @param zones
	 * @param h
	 * @param s
	 * @param v
	 * @return http status 200
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@GET
	@Path("/color/hsv")
	@Produces(MediaType.TEXT_HTML)
	public Response setHSV(@QueryParam(value = "zone") List<Integer> zones, @QueryParam(value = "h") int h,
			@QueryParam(value = "s") int s, @QueryParam(value = "v") int v) throws IOException, InterruptedException {
		try {
			api.setHSV(zones, h, s, v);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}


	/**
	 * Path: /color/rgbw
	 * 
	 * @param zones
	 * @param r
	 * @param g
	 * @param b
	 * @param maxBrightness
	 * @return http status 200
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@GET
	@Path("/color/rgbw")
	@Produces(MediaType.TEXT_HTML)
	public Response setRGBWithWhiteChannel(@QueryParam(value = "zone") List<Integer> zones, @QueryParam(value = "r") int r,
			@QueryParam(value = "g") int g, @QueryParam(value = "b") int b,
			@QueryParam(value = "maxBrightness") boolean maxBrightness) throws IOException, InterruptedException {
		try {
			api.setRGBWithWhiteChannel(zones, r, g, b, maxBrightness);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}


	/**
	 * Path: /color/hsvw
	 * 
	 * @param zones
	 * @param h
	 * @param s
	 * @param v
	 * @param maxBrightness
	 * @return http status 200
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@GET
	@Path("/color/hsvw")
	@Produces(MediaType.TEXT_HTML)
	public Response setHSVwithWihiteChannel(@QueryParam(value = "zone") List<Integer> zones, @QueryParam(value = "h") int h,
			@QueryParam(value = "s") int s, @QueryParam(value = "v") int v,
			@QueryParam(value = "maxBrightness") boolean maxBrightness) throws IOException, InterruptedException {
		try {
			api.setHSVwithWihiteChannel(zones, h, s, v, maxBrightness);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}


	/**
	 * Path: color/r
	 * 
	 * @param zones
	 * @param value
	 * @return http status 200
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@GET
	@Path("/color/r")
	@Produces(MediaType.TEXT_HTML)
	public Response setR(@QueryParam(value = "zone") List<Integer> zones, @QueryParam(value = "value") int value)
			throws IOException, InterruptedException {
		try {
			api.setR(zones, value);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}


	/**
	 * Path: /color/g
	 * 
	 * @param zones
	 * @param value
	 * @return http status 200
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@GET
	@Path("/color/g")
	@Produces(MediaType.TEXT_HTML)
	public Response setG(@QueryParam(value = "zone") List<Integer> zones, @QueryParam(value = "value") int value)
			throws InterruptedException, IOException {
		try {
			api.setG(zones, value);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}


	/**
	 * Path: /color/b
	 * 
	 * @param zones
	 * @param value
	 * @return http status 200
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@GET
	@Path("/color/b")
	@Produces(MediaType.TEXT_HTML)
	public Response setB(@QueryParam(value = "zone") List<Integer> zones, @QueryParam(value = "value") int value)
			throws InterruptedException, IOException {
		try {
			api.setB(zones, value);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}

	}


	/**
	 * Path: /color/w
	 * 
	 * @param zones
	 * @param value
	 * @return http status 200
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@GET
	@Path("/color/w")
	@Produces(MediaType.TEXT_HTML)
	public Response setW(@QueryParam(value = "zone") List<Integer> zones, @QueryParam(value = "value") int value)
			throws InterruptedException, IOException {
		try {
			api.setW(zones, value);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}


	/**
	 * Path: /control/reset
	 * 
	 * @param zones
	 * @return http status 200
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@GET
	@Path("/control/reset")
	@Produces(MediaType.TEXT_HTML)
	public Response resetColor(@QueryParam(value = "zone") List<Integer> zones) throws IOException, InterruptedException {
		try {
			api.resetColor(zones);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}


	/**
	 * Path: /control/togglePower
	 * 
	 * @param powerState
	 * @return http status 200
	 * @throws IOException
	 */
	@GET
	@Path("/control/togglePower")
	@Produces(MediaType.TEXT_HTML)
	public Response togglePower(@QueryParam(value = "powerState") boolean powerState) throws IOException {
		try {
			api.togglePower(powerState);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}


	/**
	 * Path: /control/togglePowerForZone
	 * 
	 * @param zones
	 * @param powerState
	 * @return http status 200
	 * @throws IOException
	 */
	@GET
	@Path("/control/togglePowerForZone")
	@Produces(MediaType.TEXT_HTML)
	public Response togglePower(@QueryParam(value = "zone") List<Integer> zones,
			@QueryParam(value = "powerState") boolean powerState) throws IOException {
		try {
			api.togglePower(zones, powerState);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}


	/**
	 * Path: /color/brightness
	 * 
	 * @param zones
	 * @param value
	 * @return http status 200
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@GET
	@Path("/color/brightness")
	@Produces(MediaType.TEXT_HTML)
	public Response setBrightness(@QueryParam(value = "zone") List<Integer> zones, @QueryParam(value = "value") int value)
			throws InterruptedException, IOException {
		try {
			api.setBrightness(zones, value);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}


	/**
	 * Path: /control/toggleColorFader
	 * 
	 * @param zones
	 * @return http status 200
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@GET
	@Path("/control/toggleColorFader")
	@Produces(MediaType.TEXT_HTML)
	public Response toggleColorFader(@QueryParam(value = "zone") List<Integer> zones) throws IOException, InterruptedException {
		try {
			api.toggleColorFader(zones);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}


	/**
	 * Path: /control/speedUpColorFader
	 * 
	 * @param zones
	 * @return http status 200
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@GET
	@Path("/control/speedUpColorFader")
	@Produces(MediaType.TEXT_HTML)
	public Response speedUpColorFader(@QueryParam(value = "zone") List<Integer> zones) throws IOException, InterruptedException {
		try {
			api.speedUpColorFader(zones);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}


	/**
	 * Path: /control/speedDownColorFader
	 * 
	 * @param zones
	 * @return http status 200
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@GET
	@Path("/control/speedDownColorFader")
	@Produces(MediaType.TEXT_HTML)
	public Response speedDownColorFader(@QueryParam(value = "zone") List<Integer> zones) throws IOException, InterruptedException {
		try {
			api.speedDownColorFader(zones);
			return Response.status(200).build();
		} catch (SocketException e) {
			System.out.println("connection to LK35 lost. trying to reconnect");
			LK35StandaloneHTTP.connect(true);
			return Response.status(500).build();
		}
	}

}
