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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.locks.ReentrantLock;

import org.ambientlight.config.device.led.HueLedPointConfiguration;
import org.ambientlight.device.drivers.ledpoint.hue.sdk.exceptions.HueSDKException;
import org.ambientlight.device.led.LedPoint;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.exception.PHBridgeNotConnectedException;
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

	protected static final int BRIDGE_UPDATE_FREQUENCY = 10;

	private Map<String, Timer> dispatcherTimer = new HashMap<String, Timer>();

	private Map<String, List<HueConnectionListener>> listener = new HashMap<String, List<HueConnectionListener>>();

	private Map<String, PHAccessPoint> bridgesList = new HashMap<String, PHAccessPoint>();

	private Map<String, Map<String, LedPoint>> inQeue = new HashMap<String, Map<String, LedPoint>>();
	private Map<String, ReentrantLock> queueLocks = new HashMap<String, ReentrantLock>();

	private PHHueSDK hueSDK;
	private static HueSDKWrapper instance;

	private String currentMacAdress = "";

	private boolean reconnect = false;
	private boolean searchAP = true;


	public static HueSDKWrapper getInstance(PHHueSDK sdk) {
		if (instance == null) {
			instance = new HueSDKWrapper(sdk);
		}
		return instance;
	}


	private HueSDKWrapper(PHHueSDK sdk) {

		hueSDK = sdk;
		hueSDK.setAppName("middleware");
		hueSDK.setDeviceName("AmbientControl");

		hueSDK.getNotificationManager().registerSDKListener(new PHSDKListener() {

			@Override
			public void onParsingErrors(List<PHHueParsingError> errors) {

				// print errors before reconnect
				System.out.println("HUESDKWrapper - onParsingErrors(): printing errors and reconnecting: ");
				for (PHHueParsingError currentError : errors) {
					System.out.println("Error: " + currentError.getAddress() + ", " + currentError.getCode() + ", "
							+ currentError.getMessage());
				}

				reconnect(currentMacAdress);
			}


			@Override
			public void onError(int errorCode, String errorDescription) {
				// print error before reconnect
				System.out.println("HUESDKWrapper - onError():" + currentMacAdress + ", " + errorCode + ", " + errorDescription);

				reconnect(currentMacAdress);
			}


			@Override
			public void onConnectionResumed(PHBridge paramPHBridge) {
			}


			@Override
			public void onConnectionLost(PHAccessPoint ap) {
				// print error before reconnect
				System.out.println("HUESDKWrapper - onConnectionLost():" + currentMacAdress);

				reconnect(currentMacAdress);
			}


			@Override
			public void onCacheUpdated(List<Integer> paramList, PHBridge paramPHBridge) {
				// do not handle this for now
			}


			@Override
			public void onBridgeConnected(PHBridge b) {
				hueSDK.setSelectedBridge(b);
				hueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);

				// create qeue and start Dispatcher
				inQeue.put(currentMacAdress, new HashMap<String, LedPoint>());
				startDispatcher(currentMacAdress);

				for (HueConnectionListener current : listener.get(currentMacAdress)) {
					current.onBridgeConnected();
				}

				System.out.println("HUESDKWrapper.onBridgeConnected(): bridge connected, dispatcher started. Hue is ready.");
			}


			@Override
			public void onAuthenticationRequired(PHAccessPoint accessPoint) {
				hueSDK.startPushlinkAuthentication(accessPoint);
				System.out.println("HUESDKWrapper: auth required! You have 30 seconds to push bridge button.");
			}


			@Override
			public void onAccessPointsFound(List<PHAccessPoint> paramList) {
				System.out.println("HUESDKWrapper: found these bridges here:");

				// stop searching for access points
				searchAP = false;

				for (PHAccessPoint current : paramList) {
					bridgesList.put(current.getMacAddress(), current);
					System.out.println("HUESDKWrapper: " + current.getMacAddress());
				}

				// try to connect to bridge if a mac was given already
				if (currentMacAdress != null && currentMacAdress.isEmpty() == false) {
					connectToMacAdress(currentMacAdress);
				}
			}
		});

		final PHBridgeSearchManager sm = (PHBridgeSearchManager) hueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);

		// search until a first ap shows up
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (searchAP) {
					System.out.println("HUESDKWrapper: searching bridges...");
					sm.search(true, false);
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}


	public synchronized void connectToMacAdress(String macAdress) {

		if (hueSDK.getSelectedBridge() != null) {
			System.out.println("HUESDKWrapper.connectToMac(): We are currently connected to this mac already: "
					+ this.currentMacAdress);
			return;
		}

		System.out.println("HUESDKWrapper.connectToMac(): want to connect to this macAdress: " + macAdress);
		this.currentMacAdress = macAdress;

		// create a lock for inQueue handlers
		queueLocks.put(macAdress, new ReentrantLock());

		// trying to connect if possible
		PHAccessPoint ap = this.bridgesList.get(macAdress);
		if (ap != null) {
			ap.setUsername("ambientControlUser");
			hueSDK.connect(ap);
		} else {
			System.out.println("HUESDKWrapper.connectToMac(): mac was not found(already). Waiting for it: " + macAdress);
		}
	}


	/**
	 * @param macAdress
	 */
	public synchronized void disconnectFromMacAdress(String macAdress) {
		List<HueConnectionListener> listeners = this.listener.get(macAdress);

		for (HueConnectionListener current : listeners) {
			current.onBridgeConnectionLost();
		}

		// disconnect
		if (listener.get(macAdress).size() == 0) {
			this.stopDispatcherTask(macAdress);
			hueSDK.disableHeartbeat(hueSDK.getSelectedBridge());
			hueSDK.disconnect(hueSDK.getSelectedBridge());
			hueSDK.setSelectedBridge(null);
			this.currentMacAdress = "";
		}
	}


	private void reconnect(String macAdress) {
		// can be called several times. because hue callback sends several errors. so stop right now if we are reconnecting
		// already or never have had a connection open to a bridge (see constructor)
		if (reconnect || searchAP)
			return;

		reconnect = true;

		// disconnect
		List<HueConnectionListener> listeners = this.listener.get(macAdress);
		for (HueConnectionListener current : listeners) {
			current.onBridgeConnectionLost();
		}

		// disconnect
		this.stopDispatcherTask(macAdress);
		try {
			hueSDK.disableHeartbeat(hueSDK.getSelectedBridge());
			hueSDK.disconnect(hueSDK.getSelectedBridge());
		} catch (PHBridgeNotConnectedException e) {
			// maybe bridge is not connected anymore. this is ok.
		}

		hueSDK.setSelectedBridge(null);

		// wait and reconnect
		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		connectToMacAdress(macAdress);

		reconnect = false;
	}


	public synchronized void registerListener(String macAdress, HueConnectionListener aListener) {
		if (macAdress != this.currentMacAdress) {
			System.out.println("HUESDKWrapper.registerListener(): Sorry. We are currently connected to this mac: "
					+ this.currentMacAdress);
		}

		if (this.listener.get(macAdress) == null) {
			this.listener.put(macAdress, new ArrayList<HueConnectionListener>());
		}

		if (aListener != null) {
			this.listener.get(macAdress).add(aListener);
		}
	}


	public synchronized void unRegisterListener(String macAdress, HueConnectionListener aListener) {
		if (aListener != null) {
			this.listener.get(macAdress).remove(aListener);
		}
	}


	protected synchronized boolean updateLight(String macAdress, String lightName, int duration, int ColorTemp, int brightness) {
		return false;
	}


	protected synchronized boolean updateLight(String macAdress, String lightName, int duration, Color color)
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
		PHLight light = null;
		List<PHLight> allLights = cache.getAllLights();
		for (PHLight current : allLights) {
			if (lightName.equals(current.getName())) {
				light = current;
				break;
			}
		}

		// if light is not reachable return false to show that updates cannot performed
		if (light == null || light.getLastKnownLightState().isReachable() == false)
			return false;

		PHLightState lightState = new PHLightState();

		// black color switches the light off
		if (Color.BLACK.equals(color)) {
			lightState.setOn(false);
		} else {
			lightState.setOn(true);
		}

		float[] hsb = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		lightState.setBrightness((int) (hsb[2] * 255));

		float xy[] = PHUtilities.calculateXYFromRGB(color.getRed(), color.getGreen(), color.getBlue(), light.getModelNumber());
		lightState.setColorMode(PHLightColorMode.COLORMODE_XY);
		lightState.setX(xy[0]);
		lightState.setY(xy[1]);

		// transition time is in 100msec values
		lightState.setTransitionTime(duration / 100);
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
	protected synchronized PHBridgeResourcesCache getLightCache(String macAdress) throws HueSDKException {

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


	private void startDispatcher(String macAdress) {
		System.out.println("HUESDKWrapper.startDispatcher(): starting timer: " + macAdress);
		Timer t = new Timer();
		t.schedule(new HueDispatcherTask(macAdress, this), 0, 1000 / BRIDGE_UPDATE_FREQUENCY);
		this.dispatcherTimer.put(macAdress, t);
	}


	private void stopDispatcherTask(String macAdress) {
		Timer timer = this.dispatcherTimer.get(macAdress);
		if (timer != null) {
			timer.cancel();
			timer.purge();
			this.dispatcherTimer.put(macAdress, null);
			System.out.println("HUESDKWrapper.stopDispatcherTask(): stopped timer: " + macAdress);
		}
	}


	/**
	 * @param lights
	 * @param macAdress
	 */
	public void dispatch(List<LedPoint> lights, String macAdress) {
		ReentrantLock lock = queueLocks.get(macAdress);

		try {
			lock.lock();
			for (LedPoint current : lights) {
				this.inQeue.get(macAdress).put(((HueLedPointConfiguration) current.configuration).id, current);
			}
		} finally {
			lock.unlock();
		}
	}


	protected Map<String, Color> getInQeue(String macAdress) {
		ReentrantLock lock = queueLocks.get(macAdress);

		try {
			lock.lock();

			Map<String, Color> result = new HashMap<String, Color>();

			Collection<LedPoint> lights = this.inQeue.get(macAdress).values();
			for (LedPoint current : lights) {
				Color c = current.getOutputResult();
				result.put(((HueLedPointConfiguration) current.configuration).id, c);
			}
			return result;

		} finally {
			lock.unlock();
		}
	}
}
