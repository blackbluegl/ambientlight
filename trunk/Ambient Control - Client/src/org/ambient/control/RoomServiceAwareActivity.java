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

import java.util.ArrayList;
import java.util.List;

import org.ambient.roomservice.RoomConfigService;
import org.ambientlight.ws.Room;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;


/**
 * @author Florian Bornkessel
 * 
 */
public abstract class RoomServiceAwareActivity extends FragmentActivity {

	public static final String LOG = "RoomServiceAwareActivity";

	private List<IRoomServiceCallbackListener> roomServiceListeners = new ArrayList<IRoomServiceCallbackListener>();

	protected RoomConfigService roomService;

	private ServiceConnection roomServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			roomService = ((RoomConfigService.MyBinder) binder).getService();
			onRoomServiceConnected();
			for (IRoomServiceCallbackListener listener : roomServiceListeners) {
				listener.onRoomServiceConnected(roomService);
			}
		}


		@Override
		public void onServiceDisconnected(ComponentName className) {
			roomService = null;
		}
	};

	private final BroadcastReceiver roomServiceUpdateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (action.equals(RoomConfigService.BROADCAST_INTENT_UPDATE_ROOMCONFIG)) {
				String roomName = intent.getExtras().getString(RoomConfigService.EXTRA_ROOM_NAME);
				Room config = (Room) intent.getExtras().getSerializable(RoomConfigService.EXTRA_ROOMCONFIG);
				Log.i(LOG, "got update for Room");
				for (IRoomServiceCallbackListener listener : roomServiceListeners) {
					listener.onRoomConfigurationChange(roomName, config);
				}
			}
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// create a handle to the service that we never close. so the service
		// will stay alive even on recreate of this activity
		startService(new Intent(this, RoomConfigService.class));

		registerReceiver(roomServiceUpdateReceiver, new IntentFilter(RoomConfigService.BROADCAST_INTENT_UPDATE_ROOMCONFIG));
		bindService(new Intent(this, RoomConfigService.class), roomServiceConnection, Context.BIND_AUTO_CREATE);
	}


	public void addServiceListener(IRoomServiceCallbackListener listener) {
		roomServiceListeners.add(listener);
	}


	public void removeServiceListener(RoomServiceAwareFragment listener) {
		roomServiceListeners.remove(listener);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(roomServiceConnection);
		unregisterReceiver(roomServiceUpdateReceiver);
	}


	protected void onRoomServiceConnected() {

	}

}
