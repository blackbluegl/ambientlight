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
import java.util.Map;
import java.util.TimerTask;

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
		// get time
		long now = System.currentTimeMillis();

		// get inQeue
		Map<String, Color> inQueue = this.wrapper.getInQeue(macAdressOfBridge);

		// and add or update new lights for next round trip
		addNewLights(inQueue, outQeue);

		// remove lights that are not in inQueue
		positionInList = removeNotUpdatedLightsFromOutQeue(inQueue, outQeue, positionInList, now);

		// remove lights that are not reachable
		try {
			PHBridgeResourcesCache cache = this.wrapper.getLightCache(macAdressOfBridge);
			positionInList = removeUnReachableLights(outQeue, cache, positionInList);
		} catch (HueSDKException e) {
			// this may happen if bridge is not available.
			// we should stop here and will be started by a new timer if bridge is up again later;
			return;
		}

		// return if nothing is left to render
		if (outQeue.isEmpty()) {
			positionInList = 0;
			return;
		}

		// get count of all lights that are not new in list and calculate transition time that is needed
		int transitionTime = calculateTransitionTimeMS(outQeue);
		while (true) {
			LightState currentLightState = getNextLightStateAndIncrementPosition();
			Color inColor = inQueue.get(currentLightState.id);

			// ignore if needed
			if (currentLightState.ignoreThisRound) {
				currentLightState.ignoreThisRound = false;
				return;
			}

			// render if needed
			if (currentLightState.mustBeThisRound) {
				currentLightState.mustBeThisRound = false;
				writeToLed(now, transitionTime, currentLightState, inColor);
				return;
			}

			// write if color has changed enough
			if (isColorChanged(currentLightState, inColor, outQeue.size(), now)) {
				writeToLed(now, transitionTime, currentLightState, inColor);
				return;
			} else {
				// render next time
				currentLightState.mustBeThisRound = true;
				currentLightState.ignoreThisRound = false;
			}
		}
	}


	/**
	 * @param now
	 * @param transitionTime
	 * @param currentLightState
	 * @param ledPointForCurrent
	 */
	protected void writeToLed(long now, int transitionTime, LightState currentLightState, Color color) {
		try {
			boolean lightUpdated = wrapper.updateLight(this.macAdressOfBridge, currentLightState.id, transitionTime, color);
			if (lightUpdated) {
				currentLightState.timeStamp = now;
				currentLightState.from = calculateActualColor(currentLightState, now);
				currentLightState.to = color;
				currentLightState.mustBeThisRound = false;
				currentLightState.ignoreThisRound = false;
				currentLightState.transitionTime = transitionTime;
			} else {
				outQeue.remove(currentLightState);
				positionInList--;
			}
			return;
		} catch (HueSDKException e) {
			outQeue.remove(currentLightState);
			positionInList--;
			return;
		}
	}


	/**
	 * iterate through outlist and set pointer to next
	 * 
	 * @return
	 */
	private LightState getNextLightStateAndIncrementPosition() {
		positionInList++;
		if (positionInList >= outQeue.size()) {
			positionInList = 0;
		}

		LightState result = this.outQeue.get(positionInList);

		return result;
	}


	/**
	 * @param inQueue
	 * @param outQeue2
	 */
	private void addNewLights(Map<String, Color> inQueue, List<LightState> outQeue) {
		for (Map.Entry<String, Color> current : inQueue.entrySet()) {

			// create template
			LightState newLightState = new LightState();
			newLightState.id = current.getKey();

			// if lightstate does not exist create it
			if (outQeue.contains(newLightState) == false) {
				newLightState.ignoreThisRound = true;
				newLightState.mustBeThisRound = true;
				newLightState.from = current.getValue();
				newLightState.to = current.getValue();
				outQeue.add(newLightState);
			}
		}
	}


	/**
	 * @param outQeue2
	 * @return
	 */
	private int calculateTransitionTimeMS(List<LightState> outQeue) {
		int result = 0;
		for (LightState current : outQeue) {
			if (current.ignoreThisRound == false) {
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
				i.remove();
				pos = calculateNewPosition(outQeue, pos, outQeue.indexOf(current));
			}
		}
		return pos;
	}


	/**
	 * calculate the new position for the pointer in list. may also return -1. this is ok as long as it is garanteed that the
	 * pointer will be set to pos+1 at the end of a round.
	 * 
	 * @param outQeue
	 * @param pos
	 * @param current
	 * @return
	 */
	protected int calculateNewPosition(List<LightState> outQeue, int pos, int removedAt) {
		if (removedAt < pos) {
			pos--;
		} else if (pos >= outQeue.size()) {
			pos = outQeue.size() - 1;
		}
		return pos;
	}


	/**
	 * if the renderer does not update the light in the meanwhile we remove the light from the outqueue to save time.
	 * 
	 * @param inQueue
	 * @param outQeue
	 * @param pos
	 */
	private int removeNotUpdatedLightsFromOutQeue(Map<String, Color> inQueue, List<LightState> outQeue, int pos, long now) {

		Iterator<LightState> i = outQeue.iterator();
		while (i.hasNext()) {
			LightState current = i.next();
			Color currentInColor = inQueue.get(current.id);
			if (currentInColor == null) {
				i.remove();
				pos = calculateNewPosition(outQeue, pos, outQeue.indexOf(current));
			} else {
				if (calculateActualColor(current, now).equals(currentInColor)) {
					i.remove();
					pos = calculateNewPosition(outQeue, pos, outQeue.indexOf(current));
				}
			}
		}

		return pos;
	}


	private boolean isColorChanged(LightState lightState, Color newColor, int queueSize, long now) {
		// switched of leds have always to be rendered
		if (Color.black.equals(newColor))
			return true;

		Color oldColor = lightState.from;

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
		if (now >= lightState.timeStamp)
			return lightState.to;
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
