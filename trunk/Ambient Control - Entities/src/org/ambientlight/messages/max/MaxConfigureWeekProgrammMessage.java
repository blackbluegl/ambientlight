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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxConfigureWeekProgrammMessage extends MaxMessage {

	public class DayEntry {

		private int hour;
		private int min;
		private float temp;


		// # Format of weekprofile: 16 bit integer (high byte first) for every
		// control point, 13 control points for every day
		// # each 16 bit integer value is parsed as
		// # int time =
		// # int hour = (time / 60) % 24;
		// # int minute = time % 60;
		// # int temperature = ;

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
			if (this.temp > MaxSetTemperatureMessage.MAX_TEMPERATUR) {
				this.temp = MaxSetTemperatureMessage.MAX_TEMPERATUR;
			} else if (this.temp < MaxSetTemperatureMessage.MIN_TEMPERATUR) {
				this.temp = MaxSetTemperatureMessage.MIN_TEMPERATUR;
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

	private List<DayEntry> entries = new ArrayList<DayEntry>();


	public MaxConfigureWeekProgrammMessage() {
		payload = new byte[25];
		setFlags(FLAGS_NONE);
		this.setMessageType(MaxMessageType.CONFIG_WEEK_PROFILE);
	}


	public boolean isSecondPart() {
		if ((payload[10] >> 4) > 0)
			return true;
		else
			return false;
	}


	public void setSecondPart(boolean secondPart) {
		payload[10] = (byte) (0x0F & payload[10]);
		if (secondPart) {
			payload[10] = (byte) ((1 << 4) | payload[10]);
		}
	}


	@Override
	public byte[] getPayload() {
		// sync entries in payload
		if (this.entries.size() > 0) {
			List<DayEntry> result = new ArrayList<DayEntry>(this.entries);
			// repeat the last entry until all slots are filled (defined by
			// Protocol)
			int amountNeeded = (this.isSecondPart() ? 6 : 7) - result.size();
			for (int i = 0; i < amountNeeded; i++) {
				result.add(result.get(result.size() - 1));
			}

			// fill in payload;
			int posAt = 11;
			for (DayEntry current : result) {
				byte[] byteValue = current.getByteCode();
				payload[posAt] = byteValue[0];
				payload[posAt + 1] = byteValue[1];
				posAt = posAt + 2;
			}
		}

		// get amount of bytes according to part type
		if (isSecondPart())
			return Arrays.copyOf(payload, 23);
		else
			return payload;
	}


	@Override
	public void setPayload(byte[] payload) {
		this.payload = payload;

		// determine type
		if (payload.length > 23) {
			setSecondPart(false);
		} else {
			setSecondPart(true);
		}

		// sync entries to payload
		entries.clear();
		for (int i = 11; i < payload.length; i = i + 2) {
			DayEntry entry = new DayEntry(payload[i], payload[i + 1]);
			if (entries.size() == 0) {
				this.entries.add(entry);
			} else {
				DayEntry last = this.entries.get(this.entries.size() - 1);
				if (last.equals(entry) == false) {
					this.entries.add(entry);
				}
			}
		}
	}


	public void addEntry(DayEntry entry) {
		if (entries.size() >= 6 && isSecondPart())
			throw new IllegalArgumentException("for second part only 6 entries are allowed!");
		if (entries.size() >= 7)
			throw new IllegalArgumentException("only 7 entries are allowed");
		this.entries.add(entry);
	}


	public DayEntry getEntry(int position) {
		return entries.get(position);
	}


	public MaxDayInWeek getDay() {
		return (MaxDayInWeek.forCode((byte) (payload[10] & 0x0F)));
	}


	public void setDay(MaxDayInWeek day) {
		payload[10] = (byte) (payload[10] & 0xF0);
		payload[10] = (byte) (day.byteValue | payload[10]);
	}


	@Override
	public String toString() {
		String parent = super.toString();
		String result = "Day: " + getDay() + "\nSecondPart: " + this.isSecondPart() + "\nEntries: " + entries;
		return parent + "\n" + result;
	}

}
