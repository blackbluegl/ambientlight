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

package org.ambient.control.climate;

import org.ambient.control.R;
import org.ambient.control.RoomServiceAwareFragment;
import org.ambientlight.config.room.RoomConfiguration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * @author Florian Bornkessel
 *
 */
public class ClimateFragment extends RoomServiceAwareFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// create the home container
		View myClimateView = inflater.inflate(R.layout.fragment_climate, null);

		return myClimateView;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambient.control.RoomServiceAwareFragment#onResumeWithServiceConnected
	 * ()
	 */
	@Override
	protected void onResumeWithServiceConnected() {
		// TODO Auto-generated method stub

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambient.control.RoomServiceAwareFragment#onRoomConfigurationChange
	 * (java.lang.String, org.ambientlight.room.RoomConfiguration)
	 */
	@Override
	public void onRoomConfigurationChange(String serverName, RoomConfiguration roomConfiguration) {
		// TODO Auto-generated method stub

	}

}
