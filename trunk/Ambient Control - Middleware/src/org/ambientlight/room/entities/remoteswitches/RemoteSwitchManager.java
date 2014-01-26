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

package org.ambientlight.room.entities.remoteswitches;

import java.io.IOException;

import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.room.entities.remoteswitches.RemoteSwitchManagerConfiguration;
import org.ambientlight.device.drivers.RemoteSwtichDeviceDriver;
import org.ambientlight.room.Persistence;


/**
 * @author Florian Bornkessel
 * 
 */
public class RemoteSwitchManager {

	private RemoteSwitchManagerConfiguration config;

	private RemoteSwtichDeviceDriver device;

	private CallBackManager callbackManager;


	public RemoteSwitchManager(RemoteSwitchManagerConfiguration config, RemoteSwtichDeviceDriver device,
			CallBackManager callbackManager) {
		this.config = config;
		this.device = device;
		this.callbackManager = callbackManager;
	}


	public void setPowerStateForRemoteSwitch(String id, boolean powerState) {

		RemoteSwitch remoteSwitch = config.remoteSwitches.get(id);

		if (config.remoteSwitches.containsKey(id) == false) {
			System.out.println("RemoteSwitchManager handleSwitchChange(): got request for unknown device: =" + id);
			return;
		}

		Persistence.beginTransaction();

		remoteSwitch.setPowerState(powerState);

		Persistence.commitTransaction();

		try {
			device.setState("ELRO", remoteSwitch.houseCode, remoteSwitch.switchingUnitCode, powerState);
		} catch (IOException e) {
			System.out.println("RemoteSwitchManager handleSwitchChange():could not change remoteswitchs powerstate!");
			e.printStackTrace();
		}

		callbackManager.roomConfigurationChanged();
	}
}
