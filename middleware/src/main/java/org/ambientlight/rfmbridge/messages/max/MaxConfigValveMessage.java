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

import org.ambientlight.config.room.entities.climate.MaxDayInWeek;
import org.ambientlight.rfmbridge.RequestMessage;
import org.ambientlight.room.entities.climate.util.MaxUtil;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxConfigValveMessage extends MaxMessage implements RequestMessage {

	public static final int DEFAULT_BOOST_DURATION_MIN = 5;
	public static final int MAX_BOOST_DURATION_MIN = 60;

	public static final int DEFAULT_BOOST_VALVE_POSITION = 80;
	public static final int MAX_BOOST_VALVE_POSITION = 100;

	public static final int MAX_VALVE_OFFSET = 255;

	public class DecalcEntry {

		public MaxDayInWeek day = MaxDayInWeek.SATURDAY;
		public int hour = 12;


		@Override
		public String toString() {
			return "DecalcDay: " + day + " Time: " + hour + ":00";
		}
	}


	public MaxConfigValveMessage() {
		this.payload = new byte[14];
		this.setMessageType(MaxMessageType.CONFIG_VALVE);
		this.setBoostDuration(DEFAULT_BOOST_DURATION_MIN);
		this.setBoostValvePosition(DEFAULT_BOOST_VALVE_POSITION);
		this.setDecalc(new DecalcEntry());
		this.setValveOffset(MaxUtil.DEFAULT_VALVE_OFFSET);
		this.setMaxValvePosition(MaxUtil.DEFAULT_MAX_VALVE_POSITION);
	}


	public void setBoostDuration(BoostDuration boostDuration) {
		payload[10] = (byte) (payload[10] & 0x1F);
		payload[10] = (byte) ((boostDuration.ordinal() << 5) | payload[10]);
	}


	public void setBoostDuration(int minutes) {
		this.setBoostDuration(BoostDuration.getClosest(minutes));
	}


	public BoostDuration getBoostDuration() {
		return BoostDuration.values()[(payload[10] & 0xFF) >> 5];
	}


	public void setBoostValvePosition(int position) {
		if (position > MAX_BOOST_VALVE_POSITION) {
			position = MAX_BOOST_VALVE_POSITION;
		}
		position = position / 5;

		payload[10] = (byte) (payload[10] & 0xE0);
		payload[10] = (byte) (payload[10] | position);
	}


	public int getBoostValvePosition() {
		return (payload[10] & 0x1F) * 5;
	}


	public void setDecalc(DecalcEntry decalc) {
		if (decalc.hour > 24) {
			decalc.hour = 24;
		}
		payload[11] = (byte) (decalc.day.byteValue << 5 | decalc.hour);
	}


	public DecalcEntry getDecalc() {
		DecalcEntry entry = new DecalcEntry();
		entry.day = MaxDayInWeek.forCode((byte) ((payload[11] & 0xFF) >> 5));
		entry.hour = payload[11] & 0x1F;
		return entry;
	}


	public void setMaxValvePosition(int position) {
		if (position > MaxUtil.MAX_VALVE_POSITION) {
			position = MaxUtil.MAX_VALVE_POSITION;
		}
		payload[12] = (byte) position;
	}


	public int getMaxValvePosition() {
		return payload[12] & 0xFF;
	}


	public void setValveOffset(int offset) {
		if (offset > MAX_VALVE_OFFSET) {
			offset = MAX_VALVE_OFFSET;
		}
		payload[13] = (byte) offset;
	}


	public int getValveOffset() {
		return payload[13] & 0xFF;
	}


	@Override
	public String toString() {
		String parent = super.toString();
		String current = "BoostDuration: " + this.getBoostDuration() + "\nBoostValvePosition: " + getBoostValvePosition() + "\n"
				+ "DecalcTime: " + getDecalc() + "\nMaxValvePosition: " + getMaxValvePosition() + "\nValveOffset: "
				+ getValveOffset();
		return parent + "\n" + current;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.rfmbridge.RequestMessage#getTimeOutSec()
	 */
	@Override
	public int getTimeOutSec() {
		return 10;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.rfmbridge.RequestMessage#getRetryCount()
	 */
	@Override
	public int getRetryCount() {
		return 10;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.rfmbridge.RequestMessage#getCorrelation()
	 */
	@Override
	public String getCorrelation() {
		if (getSequenceNumber() == null)
			return null;
		return getSequenceNumber().toString();
	}
}
