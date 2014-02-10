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

package org.ambient.control;

import org.ambientlight.config.room.RoomConfiguration;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * @author Florian Bornkessel
 *
 */
public class RoomConfigurationParceable implements Parcelable {

	public RoomConfiguration roomConfiguration;


	public RoomConfigurationParceable(RoomConfiguration roomConfiguration) {
		this.roomConfiguration = roomConfiguration;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel paramParcel, int paramInt) {
		paramParcel.writeSerializable(roomConfiguration);

	}

}
