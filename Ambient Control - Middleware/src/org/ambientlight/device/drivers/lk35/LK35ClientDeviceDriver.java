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

package org.ambientlight.device.drivers.lk35;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.drivers.LK35CLientDeviceConfiguration;
import org.ambientlight.device.drivers.LedPointDeviceDriver;
import org.ambientlight.device.drivers.RemoteHostConfiguration;
import org.ambientlight.device.led.LedPoint;
import org.lk35.api.LK35ColorHandler;
import org.lk35.api.LK35ColorHandlerImpl;
import org.lk35.api.LK35DeviceHandler;
import org.lk35.api.LK35DeviceHandlerImpl;


/**
 * @author Florian BornkesselStripe stripe
 * 
 */
public class LK35ClientDeviceDriver implements LedPointDeviceDriver {

	LK35CLientDeviceConfiguration config = null;

	List<LedPoint> points = new ArrayList<LedPoint>();

	OutputStream os = null;

	LK35DeviceHandler connection;
	LK35ColorHandler colorHandler;


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.device.drivers.LedStripeDeviceDriver#setConfiguration
	 * (org
	 * .ambientlight.device.drivers.MultiStripeOverEthernetClientDeviceConfiguration
	 * )
	 */
	@Override
	public void setConfiguration(RemoteHostConfiguration configuration) {
		this.config = (LK35CLientDeviceConfiguration) configuration;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.device.drivers.LedStripeDeviceDriver#connect()
	 */
	@Override
	public void connect() throws UnknownHostException, IOException {
		this.connection = new LK35DeviceHandlerImpl();
		this.os = connection.connect(this.config.hostName, this.config.port);
		colorHandler = new LK35ColorHandlerImpl(this.os);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.device.drivers.LedStripeDeviceDriver#closeConnection()
	 */
	@Override
	public void closeConnection() {
		try {
			this.connection.disconnect();
		} catch (IOException e) {
			// we cannot do much here
			e.printStackTrace();
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.device.drivers.LedStripeDeviceDriver#writeData()
	 */
	@Override
	public void writeData() throws IOException {

		if (os == null)
			throw new IOException("Outputwriter was not prepared. Cannot write Data to LK35-device!");

		List<LedPoint> done = new ArrayList<LedPoint>();

		for (LedPoint current : points) {
			if (done.contains(current)) {
				continue;
			}
			done.add(current);

			List<Integer> zones = new ArrayList<Integer>();
			zones.add(current.configuration.port);

			for (LedPoint currentOther : points) {
				if (done.contains(currentOther)) {
					continue;
				}

				if (currentOther.getOutputResult().equals(current.getOutputResult())) {
					zones.add(currentOther.configuration.port);
					done.add(currentOther);
				}
			}
			Color color = new Color(current.getOutputResult());
			try {
				this.colorHandler.setRGBWithWhiteChannel(zones, color.getRed(), color.getGreen(), color.getBlue(), false);
			} catch (InterruptedException e) {
				// do nothing here
				e.printStackTrace();
			}
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.device.drivers.LedPointDeviceDriver#getLedPoints()
	 */
	@Override
	public List<LedPoint> getLedPoints() {
		// TODO Auto-generated method stub
		return this.points;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.device.drivers.LedPointDeviceDriver#attachLedPoint(org
	 * .ambientlight.device.led.LedPoint)
	 */
	@Override
	public void attachLedPoint(LedPoint ledPoint) {
		this.points.add(ledPoint);
	}
}
