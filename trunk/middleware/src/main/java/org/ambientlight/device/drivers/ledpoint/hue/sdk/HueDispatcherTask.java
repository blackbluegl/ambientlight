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
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.ambientlight.device.drivers.ledpoint.hue.sdk.exceptions.HueSDKException;

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

	LightState renderThisRound = null;


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

		// get time for all pending operations
		long now = System.currentTimeMillis();

		// get inQeue
		Map<String, Color> inQueue = this.wrapper.getInQeue(macAdressOfBridge);

		// and add or update new lights for next round trip
		addNewLights(inQueue, outQeue);

		// ignore lights that are not reachable
		try {
			PHBridgeResourcesCache cache = this.wrapper.getLightCache(macAdressOfBridge);
			filterUnReachableLights(outQeue, cache);
		} catch (HueSDKException e) {
			// this may happen if bridge is not available.
			// we should stop here and will be started by a new timer if bridge is up again later;
			return;
		}

		// define the rendering order
		this.renderThisRound = scoreLights(inQueue, outQeue, now);

		// get count of all lights that are not new in list and calculate transition time that is needed
		int transitionTime = calculateTransitionTimeMS(outQeue);

		if (this.renderThisRound == null)
			return;

		Color inColor = inQueue.get(renderThisRound.id);

		// write led. This timeslot is used and so we return and next wait for next call.
		writeToLed(now, transitionTime, renderThisRound, inColor);
		// reset score. to render the other ligts first.
		renderThisRound.renderScore = 0;
		renderThisRound = null;
		return;
	}


	/**
	 * @param now
	 * @param transitionTime
	 * @param currentLightState
	 * @param ledPointForCurrent
	 */
	protected void writeToLed(long now, int transitionTime, LightState currentLightState, Color color) {
		try {
			// strange hue behavior fix
			int r = color.getRed() == 255 ? 254 : color.getRed();
			int g = color.getGreen() == 255 ? 254 : color.getGreen();
			int b = color.getBlue() == 255 ? 254 : color.getBlue();
			Color colorForOutput = new Color(r, g, b);

			System.out.println("HueDispatcherTask.writeToLed(): " + currentLightState.id + " " + colorForOutput);
			boolean lightUpdated = wrapper.updateLight(this.macAdressOfBridge, currentLightState.id, transitionTime,
					colorForOutput);

			if (lightUpdated) {
				currentLightState.timeStamp = now;
				currentLightState.from = calculateActualColor(currentLightState, now);
				currentLightState.to = color;
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
				newLightState.ignoreThisRound = false;
				newLightState.from = Color.BLACK;
				newLightState.to = Color.BLACK;
				outQeue.add(newLightState);
			}
		}
	}


	/**
	 * @param outQeue
	 * @return
	 */
	private int calculateTransitionTimeMS(List<LightState> outQeue) {
		int result = 0;
		for (LightState current : outQeue) {
			if (current.renderScore > 0) {
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
	 * @param outQeue
	 * @param cache
	 * @param pos
	 */
	private void filterUnReachableLights(List<LightState> outQeue, PHBridgeResourcesCache cache) {

		for (LightState current : outQeue) {
			boolean found = false;

			for (PHLight currentPhillipsLight : cache.getAllLights()) {

				if (currentPhillipsLight.getName() != null && currentPhillipsLight.getName().equals(current.id)
						&& currentPhillipsLight.getLastKnownLightState() != null
						&& currentPhillipsLight.getLastKnownLightState().isReachable()) {
					found = true;
					break;
				}
			}

			if (found == false) {
				current.ignoreThisRound = true;
			}
		}
	}


	/**
	 * if the renderer does not update the light in the meanwhile we remove the light from the outqueue to save time.
	 * 
	 * @param inQueue
	 * @param outQeue
	 * @param pos
	 */
	private LightState scoreLights(Map<String, Color> inQueue, List<LightState> outQeue, long now) {

		LightState renderThisRound = this.renderThisRound;

		for (LightState current : outQeue) {

			if (current.ignoreThisRound) {
				current.ignoreThisRound = false;
				current.renderScore = 0;
				continue;
			}

			Color currentInColor = inQueue.get(current.id);

			// ignore if no update in inqueue
			if (currentInColor == null) {
				current.renderScore = 0;
				continue;
			}

			int score = isColorChanged(current, currentInColor, outQeue.size(), now);

			current.renderScore += score;

			if (current.renderScore > 0 && (renderThisRound == null || current.renderScore >= renderThisRound.renderScore)) {
				renderThisRound = current;
			}
		}
		return renderThisRound;
	}


	private int isColorChanged(LightState lightState, Color newColor, int queueSize, long now) {

		Color oldColor = lightState.from;

		// calculate actual color
		if (now < lightState.timeStamp + lightState.transitionTime) {
			oldColor = calculateActualColor(lightState, now);
		}

		return getDifference(oldColor, newColor);
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
