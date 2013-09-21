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

package org.ambient.roomservice.boot;

import org.ambient.roomservice.RoomConfigService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * @author Florian Bornkessel
 *
 */
public class StartRoomConfigServiceReceiver extends  BroadcastReceiver {

	public static final String INTENT_START = "org.ambientcontrol.callback.startService";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent myIntent = new Intent(context, RoomConfigService.class);
		context.startService(myIntent);
	}

}
