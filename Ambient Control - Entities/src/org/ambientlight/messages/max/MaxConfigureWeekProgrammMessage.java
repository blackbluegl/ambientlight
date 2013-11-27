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


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxConfigureWeekProgrammMessage extends MaxMessage {



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
