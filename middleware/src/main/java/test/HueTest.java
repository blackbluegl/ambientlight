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

package test;

import java.util.List;
import java.util.Map;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLight.PHLightColorMode;
import com.philips.lighting.model.PHLightState;


/**
 * @author Florian Bornkessel
 * 
 */
public class HueTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final PHHueSDK phHueSDK = PHHueSDK.getInstance();
		phHueSDK.setAppName("middleware");
		phHueSDK.setDeviceName("AmbientControl");

		// Local SDK Listener
		PHSDKListener listener = new PHSDKListener() {

			@Override
			public void onAccessPointsFound(List<PHAccessPoint> accessPoint) {
				// Handle your bridge search results here. Typically if multiple results are returned you will want to display
				// them in a list
				// and let the user select their bridge. If one is found you may opt to connect automatically to that bridge.
				if (accessPoint != null) {
					for (PHAccessPoint current : accessPoint) {
						System.out.println(current.getIpAddress());
						System.out.println(current.getUsername());
						System.out.println(current.getMacAddress());
						current.setUsername("ambientControlUser");
						phHueSDK.connect(current);

					}
				}

			}


			@Override
			public void onCacheUpdated(List cacheNotificationsList, PHBridge bridge) {
				// Here you receive notifications that the BridgeResource Cache was updated. Use the PHMessageType to
				// check which cache was updated, e.g.
				if (cacheNotificationsList.contains(PHMessageType.LIGHTS_CACHE_UPDATED)) {
					System.out.println("Lights Cache Updated ");
				}
			}


			@Override
			public void onBridgeConnected(PHBridge b) {
				phHueSDK.setSelectedBridge(b);
				phHueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
				// Here it is recommended to set your connected bridge in your sdk object (as above) and start the heartbeat.
				// At this point you are connected to a bcurrentridge so you should pass control to your main program/activity.
				// Also it is recommended you store the connected IP Address/ Username in your app here. This will allow easy
				// automatic connection on subsequent use.

				PHBridgeResourcesCache cache = phHueSDK.getSelectedBridge().getResourceCache();
				// And now you can get any resource you want, for example:
				List<PHLight> myLights = cache.getAllLights();
				Map<String, PHLight> result = cache.getLights();
				for (PHLight current : myLights) {
					System.out.println(current.getIdentifier());
					System.out.println(current.getName());
				}
				System.out.println(myLights.get(0).getLastKnownLightState().isReachable());

				float xy[] = PHUtilities.calculateXYFromRGB(255, 0, 0, myLights.get(0).getModelNumber());

				PHLightState lightState = new PHLightState();
				// lightState.setOn(true);
				lightState.setTransitionTime(1);
				lightState.setBrightness(0);
				lightState.setColorMode(PHLightColorMode.COLORMODE_XY);
				lightState.setX(xy[0]);
				lightState.setY(xy[1]);
				// lightState.setCt(155);
				b.updateLightState(myLights.get(0), lightState);
				b.updateLightState(myLights.get(1), lightState);
				b.updateLightState(myLights.get(2), lightState);
				// b.setLightStateForDefaultGroup(lightState);
				b.updateLightState(myLights.get(0), lightState, new PHLightListener() {

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						System.out.println("ssuccess");
					}


					@Override
					public void onStateUpdate(Map<String, String> arg0, List<PHHueError> arg1) {
						System.out.println("status update");

					}


					@Override
					public void onError(int code, String message) {

						System.out.println("on error: " + code + " " + message);

					}


					@Override
					public void onSearchComplete() {
						// TODO Auto-generated method stub

					}


					@Override
					public void onReceivingLights(List<PHBridgeResource> arg0) {
						// TODO Auto-generated method stub

					}


					@Override
					public void onReceivingLightDetails(PHLight arg0) {
						// TODO Auto-generated method stub

					}
				});


				long now = System.currentTimeMillis();

				for (int y = 0; y < 128; y++) {
					for (int i = 0; i < 25; i++) {
						try {
							Thread.sleep(150);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// lightState.setOn(true);
						lightState.setTransitionTime(1);
						lightState.setBrightness(i * 10);
						lightState.setX(xy[0]);
						lightState.setY(xy[1]);
						b.updateLightState(myLights.get(0), lightState);
						b.updateLightState(myLights.get(1), lightState);
						b.updateLightState(myLights.get(2), lightState);
						System.out.println(System.currentTimeMillis() - now);
					}
				}
			}


			@Override
			public void onAuthenticationRequired(PHAccessPoint accessPoint) {
				phHueSDK.startPushlinkAuthentication(accessPoint);
				// Arriving here indicates that Pushlinking is required (to prove the User has physical access to the bridge).
				// Typically here
				// you will display a pushlink image (with a timer) indicating to to the user they need to push the button on
				// their bridge within 30 seconds.
				System.out.println("auth required");
			}


			@Override
			public void onConnectionResumed(PHBridge bridge) {
				System.out.println("connection resumed");
			}


			@Override
			public void onConnectionLost(PHAccessPoint accessPoint) {
				// Here you would handle the loss of connection to your bridge.
				System.out.println("connection lost");
			}


			@Override
			public void onError(int code, final String message) {
				// Here you can handle events such as Bridge Not Responding, Authentication Failed and Bridge Not Found
				System.out.println("got error: " + code + " " + message);
			}


			@Override
			public void onParsingErrors(List parsingErrorsList) {
				// Any JSON parsing errors are returned here. Typically your program should never return these.
			}
		};

		phHueSDK.getNotificationManager().registerSDKListener(listener);

		PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
		sm.search(true, true);

		// Remember to disable the heartbeat when exiting your app
		// phHueSDK.disableAllHeartbeat();
		// PHAccessPoint test = new PHAccessPoint();
		// test.setIpAddress("127.0.0.1:8000");
		// test.setUsername("newdeveloper");
		// phHueSDK.connect(test);
	}

}
