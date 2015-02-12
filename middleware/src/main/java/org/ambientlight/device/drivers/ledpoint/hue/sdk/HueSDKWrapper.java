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

package org.ambientlight.device.drivers.ledpoint.hue.sdk;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.ambientlight.device.led.LedPoint;
import org.ambientlight.device.led.color.Color64Bit;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLight.PHLightColorMode;
import com.philips.lighting.model.PHLightState;


/**
 * @author Florian Bornkessel
 * 
 */
public class HueSDKWrapper {

	private Map<String, Timer> dispatcherTimer = new HashMap<String, Timer>();

	protected static final int BRIDGE_UPDATE_FREQUENCY = 10;

	private Map<String, PHAccessPoint> bridgesList = new HashMap<String, PHAccessPoint>();

	private PHHueSDK hueSDK;

	private String currentMacAdress = "";

	private List<HueListener> listener = new ArrayList<HueListener>();

	private Map<String, List<LedPoint>> inQeue = new ConcurrentHashMap<String, List<LedPoint>>();


	public HueSDKWrapper(PHHueSDK sdk) {

		hueSDK = sdk;
		hueSDK.setAppName("middleware");
		hueSDK.setDeviceName("AmbientControl");

		hueSDK.getNotificationManager().registerSDKListener(new PHSDKListener() {

			@Override
			public void onParsingErrors(List<PHHueParsingError> errors) {

				// print errors
				System.out.println("HUESDKWrapper - onParsingErrors(): printing errors and reconnecting: ");
				for (PHHueParsingError currentError : errors) {
					System.out.println("Error: " + currentError.getAddress() + ", " + currentError.getCode() + ", "
							+ currentError.getMessage());
				}

				// disconnect
				disconnectFromMacAdress(currentMacAdress);

				// wait and reconnect
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				connectToMacAdress(currentMacAdress);
			}


			@Override
			public void onError(int errorCode, String errorDescription) {
				// print error
				System.out.println("HUESDKWrapper - onError():" + currentMacAdress + ", " + errorCode + ", " + errorDescription);

				// disconnect
				disconnectFromMacAdress(currentMacAdress);

				// wait and reconnect
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				connectToMacAdress(currentMacAdress);
			}


			@Override
			public void onConnectionResumed(PHBridge paramPHBridge) {
				startDispatcher(currentMacAdress);
				for (HueListener current : listener) {
					current.onBridgeConnected();
				}
			}


			@Override
			public void onConnectionLost(PHAccessPoint paramPHAccessPoint) {
				dispatcherTimer.get(currentMacAdress).cancel();
				for (HueListener current : listener) {
					current.onBridgeConnectionLost();
				}
			}


			@Override
			public void onCacheUpdated(List<Integer> paramList, PHBridge paramPHBridge) {
				// do not handle this for now
			}


			@Override
			public void onBridgeConnected(PHBridge b) {
				hueSDK.setSelectedBridge(b);
				hueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);

				for (HueListener current : listener) {
					current.onBridgeConnected();
				}
				startDispatcher(currentMacAdress);
				System.out.println("HUESDKWrapper.onBridgeConnected(): bridge connected, dispatcher started.");
			}


			@Override
			public void onAuthenticationRequired(PHAccessPoint accessPoint) {
				hueSDK.startPushlinkAuthentication(accessPoint);
				System.out.println("HUESDKWrapper: auth required! You have 30 seconds to activate ambientcontrol.");
			}


			@Override
			public void onAccessPointsFound(List<PHAccessPoint> paramList) {
				System.out.println("HUESDKWrapper: found these bridges here:");
				for (PHAccessPoint current : paramList) {
					bridgesList.put(current.getMacAddress(), current);
					System.out.println("HUESDKWrapper: " + current.getMacAddress());
				}

				// try to connect to bridge directly if a mac was given already
				if (currentMacAdress != null && currentMacAdress.isEmpty() == false) {
					connectToMacAdress(currentMacAdress);
				}
			}
		});

		PHBridgeSearchManager sm = (PHBridgeSearchManager) hueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
		sm.search(true, false);
	}


	public synchronized void connectToMacAdress(String macAdress) {

		if (hueSDK.getSelectedBridge() != null) {
			System.out.println("HUESDKWrapper.connectToMac(): Sorry. We are currently connected to this mac already: "
					+ this.currentMacAdress);
			return;
		}

		System.out.println("HUESDKWrapper.connectToMac(): want to connect to this macAdress" + macAdress);
		this.currentMacAdress = macAdress;

		// trying to connect if possible
		PHAccessPoint ap = this.bridgesList.get(macAdress);
		if (ap != null) {
			hueSDK.connect(ap);
			System.out.println("HUESDKWrapper.connectToMac(): connected to this mac" + macAdress);
		} else {
			System.out.println("HUESDKWrapper.connectToMac(): mac was not found(already). Waiting for it" + macAdress);
		}
	}


	public synchronized void registerListener(String macAdress, HueListener aListener) {
		if (macAdress != this.currentMacAdress) {
			System.out.println("HUESDKWrapper.registerListener(): Sorry. We are currently connected to this mac: "
					+ this.currentMacAdress);
		}
		if (aListener != null) {
			this.listener.add(aListener);
			this.inQeue.put(macAdress, new ArrayList<LedPoint>());
		}
	}


	public synchronized void unRegisterListener(HueListener aListener) {
		if (aListener != null) {
			this.listener.remove(aListener);
		}
	}


	private synchronized boolean updateLight(String macAdress, String lightName, int duration, int ColorTemp, int brightness) {
		return false;
	}


	private synchronized boolean updateLight(String macAdress, String lightName, int duration, Color64Bit color)
			throws HueSDKException {
		if (currentMacAdress.equals(macAdress) == false) {
			System.out.println("HUESDKWrapper.updateLight(): Sorry. We are currently connected to this mac: "
					+ this.currentMacAdress);
			throw new HueSDKException("Connected to different bridge. HueSDK supports only one bridge at the moment.");
		}

		if (hueSDK.getSelectedBridge() == null) {
			System.out.println("HUESDKWrapper.updateLight(): Sorry. No current bridge set for mac: " + this.currentMacAdress);
			throw new HueSDKException("No bridge is selected");
		}

		PHBridgeResourcesCache cache = getLightCache(macAdress);

		PHLight light = cache.getLights().get(lightName);

		// if light is not reachable return false to show that updates cannot performed
		if (light == null || light.getLastKnownLightState().isReachable() == false)
			return false;

		PHLightState lightState = new PHLightState();

		Color rgb = color.getColor();

		if (Color.BLACK.equals(rgb)) {
			lightState.setOn(false);
		} else {
			lightState.setOn(true);
		}

		float[] hsb = new float[3];
		Color.RGBtoHSB(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), hsb);
		lightState.setBrightness((int) hsb[3] * 255);

		float xy[] = PHUtilities.calculateXYFromRGB(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), light.getModelNumber());
		lightState.setColorMode(PHLightColorMode.COLORMODE_XY);
		lightState.setX(xy[0]);
		lightState.setY(xy[1]);

		// transition time is in 10msec values
		lightState.setTransitionTime(duration / 10);
		try {
			hueSDK.getSelectedBridge().updateLightState(light, lightState);
		} catch (Exception e) {
			System.out.println("HUESDKWrapper.updateLight(): could not update Lightstate: " + this.currentMacAdress);
			e.printStackTrace();
			throw new HueSDKException("lightState could not be updated");
		}
		return true;
	}


	/**
	 * @param macAdress
	 * @return
	 * @throws HueSDKException
	 */
	protected PHBridgeResourcesCache getLightCache(String macAdress) throws HueSDKException {

		if (hueSDK.getSelectedBridge() == null) {
			System.out.println("HUESDKWrapper.updateLight(): Sorry. No current bridge set for mac: " + this.currentMacAdress);
			throw new HueSDKException("No bridge is selected");
		}

		PHBridgeResourcesCache cache = hueSDK.getSelectedBridge().getResourceCache();
		if (cache == null) {
			System.out.println("HUESDKWrapper.updateLight(): cache is null for: " + this.currentMacAdress);
			throw new HueSDKException("cache is null");
		}

		return cache;
	}


	/**
	 * @param macAdress
	 */
	public synchronized void disconnectFromMacAdress(String macAdress) {
		// disconnect

		this.stopDispatcher(macAdress);
		hueSDK.disableAllHeartbeat();
		hueSDK.disconnect(hueSDK.getSelectedBridge());
		hueSDK.setSelectedBridge(null);
		this.currentMacAdress = "";
	}


	/**
	 * @param lights
	 * @param macAdress
	 */
	public void dispatch(List<LedPoint> lights, String macAdress) {
		this.inQeue.put(macAdress, lights);
	}


	private void startDispatcher(String macAdress) {
		Timer t = new Timer();
		t.schedule(new HueDispatcherTask(macAdress, this), 0, 1000 / BRIDGE_UPDATE_FREQUENCY);
		this.dispatcherTimer.put(macAdress, t);
	}


	private void stopDispatcher(String macAdress) {
		this.dispatcherTimer.get(macAdress).cancel();
	}


	protected Map<String, List<LedPoint>> getInQeue() {
		return inQeue;
	}
}
