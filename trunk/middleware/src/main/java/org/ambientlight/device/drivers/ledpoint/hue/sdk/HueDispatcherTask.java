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
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import org.ambientlight.device.led.LedPoint;

import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHLight;


/**
 * @author Florian Bornkessel
 * 
 */
public class HueDispatcherTask extends TimerTask {

	List<LightState> outQeue = new ArrayList<LightState>();
	HueSDKWrapper wrapper;
	String macAdressOfBridge;
	// int queueLength = 0;
	int positionInList = 0;


	/**
	 * @param hueSDKWrapper
	 */
	public HueDispatcherTask(String macAdress, HueSDKWrapper hueSDKWrapper) {
		this.wrapper = hueSDKWrapper;
		this.macAdressOfBridge = macAdress;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		// get remembered position in list
		int pos = this.positionInList;

		// get inQeue
		List<LedPoint> inQueue = this.wrapper.getInQeue().get(macAdressOfBridge);

		// and add new lights at the end
		addNewLights(inQueue, outQeue);

		// remove lights that are not in inQueue
		pos = pos - removeNotUpdatedLightsFromOutQeue(inQueue, outQeue, pos);
		if (pos > outQeue.size() - 1) {
			pos = 0;
		}

		// remove lights that are not reachable
		try {
			PHBridgeResourcesCache cache = this.wrapper.getLightCache(macAdressOfBridge);
			pos = pos - removeUnReachableLights(outQeue, cache, pos);
			if (pos > outQeue.size() - 1) {
				pos = 0;
			}
		} catch (HueSDKException e) {
			// this may happen if bridge is not available.
			// we should stop here and wait to be started by a new timer if bridge is up again later;
			return;
		}

		// return if nothing is left todo
		if (outQeue.isEmpty())
			return;

		// get count of all lights that are not new in list
		int transitionTime = calculateTransitionTime(outQeue);

		// set current with small changes one behind until the next one until a greater change was found - therefore calculate
		// actual color regarding
		LedPoint ledPointForCurrent = getLedpointFor(outQeue.get(pos));
		boolean hasSmallChanges =

				// if current light is new. set flag to false and iterate to next light. and so on. update qeueLenght
				// colors and transitiontime
				// calculate transition time depending on the amount of all lights in queue
				// persist duration and timestamp and color of current light
				// write lightstate of current light - ignore exceptions. bridge should recover automatically.

				// increment or reset position in list
				this.positionInList = pos + 1;
		if (this.positionInList >= outQeue.size()) {
			this.positionInList = 0;
		}
	}


	/**
	 * @param inQueue
	 * @param outQeue2
	 */
	private void addNewLights(List<LedPoint> inQueue, List<LightState> outQeue2) {
		// TODO Auto-generated method stub
		implementieren
	}


	/**
	 * @param outQeue2
	 * @return
	 */
	private int calculateTransitionTime(List<LightState> outQeue) {
		int result = 0;
		for (LightState current : outQeue) {
			if (current.newInRound == false) {
				result++;
			}
		}

		if (result == 0) {
			result = 1;
		}
		return (int) (result * (1000 / (float) HueSDKWrapper.BRIDGE_UPDATE_FREQUENCY));
	}


	/**
	 * we do not need any lights in queue that are not reachable.
	 * 
	 * @param outQeue2
	 * @param cache
	 * @param pos
	 */
	private int removeUnReachableLights(List<LightState> outQeue, PHBridgeResourcesCache cache, int pos) {
		int moveBackInList = 0;

		Iterator<LightState> i = outQeue.iterator();
		while (i.hasNext()) {

			LightState current = i.next();
			boolean found = false;
			for (PHLight currentPhillipsLight : cache.getAllLights()) {
				if (currentPhillipsLight.getName() != null && currentPhillipsLight.getName().equals(current.id)) {
					if (currentPhillipsLight.getLastKnownLightState() != null
							&& currentPhillipsLight.getLastKnownLightState().isReachable()) {
						found = true;
					}
					break;
				}
			}

			if (found == false) {
				if (outQeue.indexOf(current) < pos) {
					moveBackInList++;
				}
				i.remove();
			}
		}
		return moveBackInList;
	}


	/**
	 * if the renderer does not update the light in the meanwhile we remove the light from the outqueue to save time.
	 * 
	 * @param inQueue
	 * @param outQeue
	 * @param pos
	 */
	private int removeNotUpdatedLightsFromOutQeue(List<LedPoint> inQueue, List<LightState> outQeue, int pos) {
		int moveBackInList = 0;

		Iterator<LightState> i = outQeue.iterator();
		while (i.hasNext()) {
			LightState current = i.next();

			boolean found = false;
			for (LedPoint currentLedPoint : inQueue) {
				if (currentLedPoint.configuration.id.equals(current.id)) {
					found = true;
					break;
				}
			}

			if (found == false) {
				if (outQeue.indexOf(current) < pos) {
					moveBackInList++;
				}
				i.remove();
			}
		}
		return moveBackInList;
	}


	private boolean isColorChanged(LightState lightState, Color newColor, int queueSize) {
		Color oldColor = lightState.from;
		long now = System.currentTimeMillis();

		// calculate actual color
		if (now < lightState.timeStamp + lightState.transitionTime) {
			oldColor = calculateActualColor(lightState, now);
		}

		int changeLevel = getDifference(oldColor, newColor);

		// little algoritm to determine if a color change is big enough to be rendered or not
		if (changeLevel > queueSize * 2)
			return true;

		return false;
	}


	protected Color calculateActualColor(LightState lightState, long now) {
		float position = (float) (now - lightState.timeStamp) / lightState.transitionTime;
		float red = (lightState.from.getRed() * (1 - position) + lightState.to.getRed() * position) / 255;
		float green = (lightState.from.getGreen() * (1 - position) + lightState.to.getGreen() * position) / 255;
		float blue = (lightState.from.getBlue() * (1 - position) + lightState.to.getBlue() * position) / 255;

		return new Color(red, green, blue);
	}


	private int getDifference(Color oldColor, Color newColor) {
		int r = Math.abs(oldColor.getRed() - newColor.getRed());
		int g = Math.abs(oldColor.getGreen() - newColor.getGreen());
		int b = Math.abs(oldColor.getBlue() - newColor.getBlue());
		return r + g + b;
	}
}
