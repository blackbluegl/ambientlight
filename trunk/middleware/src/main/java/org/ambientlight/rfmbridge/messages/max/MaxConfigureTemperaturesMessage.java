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

package org.ambientlight.rfmbridge.messages.max;

import org.ambientlight.room.entities.climate.util.MaxUtil;

/**
 * @author Florian Bornkessel
 * 
 */
public class MaxConfigureTemperaturesMessage extends MaxMessage {

	public final static float MIN_OFFSET = -3.5f;
	public final static float MAX_OFFSET = 3.5f;
	public final static int MAX_WINDOW_OPEN_TIME = 60;
	public final static float DEFAULT_COMFORT_TEMPERATUR = 21.0f;
	public final static float DEFALUT_ECO_TEMPERATUR = 17.0f;
	public final static float DEFAULT_WINDOW_OPEN_TEMPERATUR = 12.0f;
	public final static int DEFAULT_WINDOW_OPEN_TIME_MIN = 15;



	public MaxConfigureTemperaturesMessage() {
		payload = new byte[17];
		setMessageType(MaxMessageType.CONFIG_TEMPERATURES);
		this.setComfortTemp(DEFAULT_COMFORT_TEMPERATUR);
		this.setEcoTemp(DEFALUT_ECO_TEMPERATUR);
		this.setMaxTemp(MaxUtil.MAX_TEMPERATURE);
		this.setMinTemp(MaxUtil.MIN_TEMPERATURE);
		this.setOffsetTemp(MaxUtil.DEFAULT_OFFSET);
		this.setWindowOpenTemp(DEFAULT_WINDOW_OPEN_TEMPERATUR);
		this.setWindowOpenTime(DEFAULT_WINDOW_OPEN_TIME_MIN);
	}


	public void setComfortTemp(float temp) {
		if (temp > MaxUtil.MAX_TEMPERATURE) {
			temp = MaxUtil.MAX_TEMPERATURE;
		}
		if (temp < MaxUtil.MIN_TEMPERATURE) {
			temp = MaxUtil.MIN_TEMPERATURE;
		}
		payload[10] = (byte) (temp * 2);
	}


	public float getComfortTemp() {
		return (payload[10] & 0xFF) / 2.0f;
	}


	public void setEcoTemp(float temp) {
		if (temp > MaxUtil.MAX_TEMPERATURE) {
			temp = MaxUtil.MAX_TEMPERATURE;
		}
		if (temp < MaxUtil.MIN_TEMPERATURE) {
			temp = MaxUtil.MIN_TEMPERATURE;
		}
		payload[11] = (byte) (temp * 2);
	}


	public float getEcoTemp() {
		return (payload[11] & 0xFF) / 2.0f;
	}


	public void setMaxTemp(float temp) {
		if (temp > MaxUtil.MAX_TEMPERATURE) {
			temp = MaxUtil.MAX_TEMPERATURE;
		}
		if (temp < MaxUtil.MIN_TEMPERATURE) {
			temp = MaxUtil.MIN_TEMPERATURE;
		}
		payload[12] = (byte) (temp * 2);
	}


	public float getMaxTemp() {
		return (payload[12] & 0xFF) / 2.0f;
	}


	public void setMinTemp(float temp) {
		if (temp > MaxUtil.MAX_TEMPERATURE) {
			temp = MaxUtil.MAX_TEMPERATURE;
		}
		if (temp < MaxUtil.MIN_TEMPERATURE) {
			temp = MaxUtil.MIN_TEMPERATURE;
		}
		payload[13] = (byte) (temp * 2);
	}


	public float getMinTemp() {
		return (payload[13] & 0xFF) / 2.0f;
	}


	public void setOffsetTemp(float temp) {
		if (temp > MAX_OFFSET) {
			temp = MAX_OFFSET;
		}
		if (temp < MIN_OFFSET) {
			temp = MIN_OFFSET;
		}
		payload[14] = (byte) ((temp + 3.5f) * 2);
	}


	public float getOffsetTemp() {
		return ((payload[14] & 0xFF) / 2.0F) - 3.5f;
	}


	public void setWindowOpenTemp(float temp) {
		if (temp > MaxUtil.MAX_TEMPERATURE) {
			temp = MaxUtil.MAX_TEMPERATURE;
		}
		if (temp < MaxUtil.MIN_TEMPERATURE) {
			temp = MaxUtil.MIN_TEMPERATURE;
		}
		payload[15] = (byte) (temp * 2);
	}


	public float getWindowOpenTemp() {
		return (payload[15] & 0xFF) / 2.0f;
	}


	public void setWindowOpenTime(int time) {
		if (time > MAX_WINDOW_OPEN_TIME) {
			time = MAX_WINDOW_OPEN_TIME;
		}
		payload[16] = (byte) (time / 5);
	}


	public int getWindowOpenTime() {
		return (payload[16] & 0xFF) * 5;
	}


	@Override
	public String toString() {
		String parent = super.toString();
		String current = "ComfortTemperature: " + getComfortTemp() + "\nEcoTemperature: " + getEcoTemp() + "\nMaxTemperature: "
				+ getMaxTemp() + "\nMinTemperature: " + getMinTemp() + "\nOffsetTemperature: " + getOffsetTemp()
				+ "\nWindowOpenTemperatur: " + getWindowOpenTemp() + "\nWindowOpenTime: " + getWindowOpenTime();
		return parent + "\n" + current;
	}
}