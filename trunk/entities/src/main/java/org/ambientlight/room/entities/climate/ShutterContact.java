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

import org.ambientlight.room.entities.climate.util.DeviceType;

import com.fasterxml.jackson.annotation.JsonIgnore;



/**
 * @author Florian Bornkessel
 * 
 */
public class ShutterContact extends MaxComponent {

	private static final long serialVersionUID = 1L;

	private boolean isOpen = false;


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.config.room.actors.MaxComponentConfiguration#getDeviceType
	 * ()
	 */
	@Override
	@JsonIgnore
	public DeviceType getDeviceType() {
		return DeviceType.SHUTTER_CONTACT;
	}


	public boolean isOpen() {
		return isOpen;
	}


	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

}
