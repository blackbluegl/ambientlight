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

package org.ambientlight.device.drivers.ledpoint.hue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.config.device.drivers.HueBridgeDeviceConfiguration;
import org.ambientlight.device.drivers.LedPointDeviceDriver;
import org.ambientlight.device.drivers.ledpoint.hue.sdk.HueListener;
import org.ambientlight.device.drivers.ledpoint.hue.sdk.HueSDKWrapper;
import org.ambientlight.device.led.LedPoint;


/**
 * @author Florian Bornkessel
 * 
 */
public class HueBridgeDeviceDriver implements LedPointDeviceDriver, HueListener {

	HueSDKWrapper wrapper;

	HueBridgeDeviceConfiguration config;

	boolean isConnected = false;

	private List<LedPoint> lights = new ArrayList<LedPoint>();


	public HueBridgeDeviceDriver(HueSDKWrapper hueSDKWrapper, HueBridgeDeviceConfiguration config) {
		this.wrapper = hueSDKWrapper;
		this.config = config;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.device.drivers.AnimateableLedDevice#connect()
	 */
	@Override
	public void connect() throws UnknownHostException, IOException {
		wrapper.connectToMacAdress(config.macAdress);
		wrapper.registerListener(config.macAdress, this);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.device.drivers.AnimateableLedDevice#closeConnection()
	 */
	@Override
	public void closeConnection() {
		wrapper.unRegisterListener(this);
		wrapper.disconnectFromMacAdress(config.macAdress);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.device.drivers.AnimateableLedDevice#writeData()
	 */
	@Override
	public void writeData() throws IOException {
		if (isConnected == false)
			return;
		wrapper.dispatch(this.lights, config.macAdress);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.device.drivers.LedPointDeviceDriver#getLedPoints()
	 */
	@Override
	public List<LedPoint> getLedPoints() {
		return this.lights;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.device.drivers.LedPointDeviceDriver#attachLedPoint(org.ambientlight.device.led.LedPoint)
	 */
	@Override
	public void attachLedPoint(LedPoint ledPoint) {
		this.lights.add(ledPoint);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.device.drivers.ledpoint.hue.sdk.HueListener#onBridgeConnected()
	 */
	@Override
	public void onBridgeConnected() {
		this.isConnected = true;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.device.drivers.ledpoint.hue.sdk.HueListener#onBridgeDisconnected()
	 */
	@Override
	public void onBridgeConnectionLost() {
		this.isConnected = false;
		System.out.println("HueBridgeDeviceDriver.onBridgeConnectionLost(): waiting for reconnect: " + config.macAdress);
	}
}
