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

package org.ambientlight.messages.max;

import java.util.concurrent.TimeUnit;


public class DayEntry {

	private int hour;
	private int min;
	private float temp;


	public DayEntry(byte high, byte low) {
		int value = MaxUtil.byteArrayToInt(new byte[] { high, low }, 0, 2);
		int timeInMinutes = (value & 0x1FF) * 5;
		temp = ((value >> 9) & 0x3F) / 2.0f;

		hour = (int) TimeUnit.MINUTES.toHours(timeInMinutes);
		min = (int) TimeUnit.MINUTES.toMinutes(timeInMinutes) - (int) TimeUnit.HOURS.toMinutes(hour);
	}


	public DayEntry(int hour, int min, float temp) {
		this.hour = hour;
		this.min = min;
		int tempInt = (int) (temp * 2);
		this.temp = tempInt / 2.0f;
		if (this.temp > MaxConfigureTemperaturesMessage.MAX_TEMPERATURE) {
			this.temp = MaxConfigureTemperaturesMessage.MAX_TEMPERATURE;
		} else if (this.temp < MaxConfigureTemperaturesMessage.MIN_TEMPERATURE) {
			this.temp = MaxConfigureTemperaturesMessage.MIN_TEMPERATURE;
		}
	}


	byte[] getByteCode() {
		int amountOfDay = (hour * 60 + min) / 5;
		int temp = (int) (this.temp * 2);
		int result = (temp << 9) | amountOfDay;
		byte intValue[] = MaxUtil.intToByteArray(result);
		return new byte[] { intValue[2], intValue[3] };
	}


	public int getHour() {
		return hour;
	}


	public int getMin() {
		return min;
	}


	public float getTemp() {
		return temp;
	}


	@Override
	public String toString() {
		return "DayEntry: until " + hour + ":" + min + ", " + temp + "°C";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hour;
		result = prime * result + min;
		result = prime * result + Float.floatToIntBits(temp);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DayEntry other = (DayEntry) obj;
		if (hour != other.hour)
			return false;
		if (min != other.min)
			return false;
		if (Float.floatToIntBits(temp) != Float.floatToIntBits(other.temp))
			return false;
		return true;
	}

}