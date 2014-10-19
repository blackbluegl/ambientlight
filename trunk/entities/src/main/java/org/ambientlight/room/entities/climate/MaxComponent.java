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

package org.ambientlight.room.entities.climate;

import java.io.Serializable;
import java.util.Date;

import org.ambientlight.room.entities.climate.util.DeviceType;

import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * @author Florian Bornkessel
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class MaxComponent implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract DeviceType getDeviceType();

	private String label;
	private int adress;
	private String firmware;
	private String serial;
	private Date lastUpdate;
	private boolean rfError = false;
	private boolean timedOut = false;
	private boolean invalidArgument = false;
	private boolean batteryLow = false;


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public int getAdress() {
		return adress;
	}


	public void setAdress(int adress) {
		this.adress = adress;
	}


	public String getFirmware() {
		return firmware;
	}


	public void setFirmware(String firmware) {
		this.firmware = firmware;
	}


	public String getSerial() {
		return serial;
	}


	public void setSerial(String serial) {
		this.serial = serial;
	}


	public Date getLastUpdate() {
		return lastUpdate;
	}


	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}


	public boolean isRfError() {
		return rfError;
	}


	public void setRfError(boolean rfError) {
		this.rfError = rfError;
	}


	public boolean isTimedOut() {
		return timedOut;
	}


	public void setTimedOut(boolean timedOut) {
		this.timedOut = timedOut;
	}


	public boolean isInvalidArgument() {
		return invalidArgument;
	}


	public void setInvalidArgument(boolean invalidArgument) {
		this.invalidArgument = invalidArgument;
	}


	public boolean isBatteryLow() {
		return batteryLow;
	}


	public void setBatteryLow(boolean batteryLow) {
		this.batteryLow = batteryLow;
	}

}