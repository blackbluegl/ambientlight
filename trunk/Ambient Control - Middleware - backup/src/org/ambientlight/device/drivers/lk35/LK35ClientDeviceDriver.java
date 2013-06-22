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
import org.ambientlight.device.drivers.LedStripeDeviceDriver;
import org.ambientlight.device.drivers.RemoteHostConfiguration;
import org.ambientlight.device.stripe.Stripe;
import org.lk35.api.LK35ColorHandler;
import org.lk35.api.LK35ColorHandlerImpl;
import org.lk35.api.LK35DeviceHandler;
import org.lk35.api.LK35DeviceHandlerImpl;


/**
 * @author Florian Bornkessel
 *
 */
// TODO zones are not stripes. zones = more than one light react to same light.
// stripes: one stripe has several lights.
public class LK35ClientDeviceDriver implements LedStripeDeviceDriver {

	LK35CLientDeviceConfiguration config = null;

	List<Stripe> stripes = new ArrayList<Stripe>();

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
	 * @see
	 * org.ambientlight.device.drivers.LedStripeDeviceDriver#getAllStripes()
	 */
	@Override
	public List<Stripe> getAllStripes() {
		// TODO Auto-generated method stub
		return stripes;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.device.drivers.LedStripeDeviceDriver#attachStripe(org
	 * .ambientlight.device.stripe.Stripe)
	 */
	@Override
	public void attachStripe(Stripe stripe) {
		this.stripes.add(stripe);
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
		Stripe stripe = this.stripes.get(0);
		for (int i = 0; i < stripe.getOutputResult().size(); i++) {
			List<Integer> zones = new ArrayList<Integer>();
			Color color = new Color(stripe.getOutputResult().get(i));
			try {
				this.colorHandler.setRGB(zones, color.getRed(), color.getGreen(), color.getBlue());
			} catch (InterruptedException e) {
				// do nothing here
				e.printStackTrace();
			}
		}
	}
}
