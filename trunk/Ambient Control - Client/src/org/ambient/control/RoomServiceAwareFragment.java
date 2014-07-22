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

	// for the case that a fragment will be created but createView is not called
	// yet it does not make sense to call onResumeWithServiceConnected which
	// refers
	// to created views. So until onResume the listener will be blocked. In a
	// normal lifecylce onResume() will call onResumeWithServiceConnected() or
	// onRoomServiceConnected() will do so. that depends on the time the service
	// is bound
	private boolean listenToServiceChange = true;


	protected abstract void onResumeWithServiceConnected();


	@Override
	public abstract void onRoomConfigurationChange(String serverName, Room roomConfiguration);


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((HomeActivity) getActivity()).addServiceListener(this);
		listenToServiceChange = false;

	}


	@Override
	public void onRoomServiceConnected(RoomConfigService service) {
		this.roomService = service;
		if (listenToServiceChange) {
			onResumeWithServiceConnected();
		}
	}


	@Override
	public void setRoomService(RoomConfigService roomService) {
		this.roomService = roomService;
	}


	@Override
	public void onResume() {
		super.onResume();
		if (roomService != null) {
			onResumeWithServiceConnected();
		}
		listenToServiceChange = true;
	}


	@Override
	public void onStop() {
		super.onStop();
		((HomeActivity) getActivity()).removeServiceListener(this);
	}

}
