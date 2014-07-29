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

import org.ambient.control.home.HomeActivity;
import org.ambient.roomservice.RoomConfigService;
import org.ambientlight.ws.Room;

import android.os.Bundle;
import android.support.v4.app.Fragment;


/**
 * @author Florian Bornkessel
 * 
 */
public abstract class RoomServiceAwareFragment extends Fragment implements IRoomServiceCallbackListener {

	protected RoomConfigService roomService = null;

	// onResumeWithServiceConnected() shall be called after the service is connected but not before onResume() is called.
	// onRoomServiceConnected() may be called before OnResume(), when the service is already started or after onResume(), if not.
	// This value is a flag to get sure that onResumeWithService() will be called not before the service is active and not before
	// onResume() would be called from the generic android lifecycle.
	private boolean listenToServiceChange = true;


	/**
	 * acts as onResume(). You are safe to access the roomConfigService here when you implement that method.
	 */
	protected abstract void onResumeWithService();


	@Override
	public abstract void onRoomConfigurationChange(String serverName, Room roomConfiguration);


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listenToServiceChange = false;
	}


	@Override
	public void onRoomServiceConnected(RoomConfigService service) {
		this.roomService = service;
		if (listenToServiceChange) {
			onResumeWithService();
		}
	}


	@Override
	public void setRoomService(RoomConfigService roomService) {
		this.roomService = roomService;
	}


	/**
	 * register this fragment as listener to changes for the onRoomConfigurationChange() callback method. Call the fragments
	 * onResumeWithService() callback or allow onRoomserviceConnected() to do so if the service is not already running in this
	 * lifecycle phase
	 */
	@Override
	public void onResume() {
		super.onResume();

		listenToServiceChange = true;

		((RoomServiceAwareActivity) getActivity()).addServiceListener(this);

		if (roomService != null) {
			onResumeWithService();
		}
	}


	@Override
	public void onPause() {
		super.onPause();
		((HomeActivity) getActivity()).removeServiceListener(this);
	}

}
