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

package org.ambient.roomservice;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ambient.rest.RestClient;
import org.ambient.roomservice.socketcallback.CallbackSocketServerRunnable;
import org.ambientlight.ws.Room;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;


/**
 * @author Florian Bornkessel
 * 
 */
public class RoomConfigService extends Service {

	public static final String BROADCAST_INTENT_UPDATE_ROOMCONFIG = "org.ambientcontrol.callback.updateRoomConfig";

	public static final String EXTRA_ROOMCONFIG = "roomConfiguration";
	public static final String EXTRA_ROOM_NAME = "roomName";

	private static final String LOG = "org.ambientcontrol.roomConfigService";

	private Map<String, Room> roomConfiguration = new HashMap<String, Room>();

	private CallbackSocketServerRunnable callbackSocketServer = null;
	private final IBinder binder = new MyBinder();

	public class MyBinder extends Binder {

		public RoomConfigService getService() {
			return RoomConfigService.this;
		}
	}

	/**
	 * the receiver is used to start and stop the socketServer and register a
	 * callback on the server based on the state of the screen.
	 */
	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (action.equals(Intent.ACTION_SCREEN_ON) && isConnectedToWifi(context)) {
				Log.i(LOG, " startCallBackServer and reload roomConfigurations because of ACTION_SCREEN_ON");
				startCallBackServer();
				initAllRoomConfigurations(true);

			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				Log.i(LOG, " stopCallBackServer because of ACTION_SCREEN_OFF");
				if (isConnectedToWifi(context)) {
					stopCallBackServer(true);
				} else {
					stopCallBackServer(false);
				}
				roomConfiguration.clear();

			} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) && isConnectedToWifi(context)) {
				// wlan on, wlan reset,fm on
				Log.i(LOG, "updateWidget because of NETWORK_STATE_CHANGED_ACTION and isConnected=true");
				initAllRoomConfigurations(true);
				startCallBackServer();

			} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) && !isConnectedToWifi(context)) {

				// fm off, wlan off, wlan lost
				Log.i(LOG, " disable because of CONNECTIVITY_ACTION and isConnected=false");
				stopCallBackServer(false);
				roomConfiguration.clear();
			}

			for (String currentRoom : roomConfiguration.keySet()) {
				notifyListener(currentRoom);
			}
		}
	};


	public Set<String> getAllRoomNames() {
		return roomConfiguration.keySet();
	}

	// update request from server
	public synchronized void updateRoomConfigForRoomName(String roomName) {
		try {
			Room roomConfig = RestClient.getRoom(roomName);
			// update Model
			roomConfiguration.put(roomName, roomConfig);
		} catch (Exception e) {
			Log.e(LOG, "updateRoomConfigForRoomName() failed. Set roomConfig to null for room: " + roomName, e);
			roomConfiguration.put(roomName, null);
		}

		// send BroadcastMessage to listeners who might be interested
		notifyListener(roomName);
	}


	/**
	 * @param serverName
	 */
	private void notifyListener(String roomName) {
		Intent intent = new Intent();
		intent.setAction(BROADCAST_INTENT_UPDATE_ROOMCONFIG);
		intent.putExtra(EXTRA_ROOMCONFIG, roomConfiguration.get(roomName));
		intent.putExtra(EXTRA_ROOM_NAME, roomName);
		sendBroadcast(intent);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}


	@Override
	public void onCreate() {
		Log.i(LOG, "onCreated Called");
		// register filter for sytem actions that will call us later
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		registerReceiver(receiver, filter);

		if (isConnectedToWifi(this) == false) {
			Log.i(LOG, "no wifi available. callback server will not be started");
			return;
		}

		// initially load all RoomConfigurations
		initAllRoomConfigurations(false);

		startCallBackServer();
	}


	private void initAllRoomConfigurations(boolean notifyListeners) {
		List<String> roomNames;

		try {
			roomNames = RestClient.getRoomNames();
		} catch (Exception e) {
			Log.e(LOG, "error could not retreive roomNames. Resetting!", e);
			roomConfiguration = new HashMap<String, Room>();
			return;
		}

		for (String currentRoom : roomNames) {
			try {
				Room config = RestClient.getRoom(currentRoom);
				roomConfiguration.put(currentRoom, config);
			} catch (Exception e) {
				Log.e(LOG, "error loading room. Ignoring this one", e);
				roomConfiguration.put(currentRoom, null);
			}
		}
	}


	@Override
	public void onDestroy() {
		Log.i(LOG, "onDestroy Called");
		unregisterReceiver(receiver);
		stopCallBackServer(true);
		super.onDestroy();
	}


	private synchronized void startCallBackServer() {

		// start socketServer
		if (callbackSocketServer == null) {
			callbackSocketServer = new CallbackSocketServerRunnable(this);
			new Thread(callbackSocketServer).start();

			String hostname = getIpAdress() + ":4321";
			for (String currentRoom : roomConfiguration.keySet()) {
				try {
					if (RestClient.registerCallback(currentRoom, hostname) == false) {
						roomConfiguration.put(currentRoom, null);
					}
				} catch (Exception e) {
					Log.e(LOG, "error trying to register callback. Ommiting server and remove the configuration.");
					roomConfiguration.put(currentRoom, null);
				}
			}
		}
	}


	private synchronized void stopCallBackServer(boolean notifyServers) {
		if (callbackSocketServer != null) {
			try {
				if (notifyServers == true) {
					String hostname = getIpAdress() + ":4321";

					for (String currentRoomName : roomConfiguration.keySet()) {
						RestClient.unregisterCallback(currentRoomName, hostname);
					}
				}
				callbackSocketServer.stop();
			} catch (IOException e) {
				Log.e(LOG, "error closing callbackSocketService");
			}
			callbackSocketServer = null;
		}
	}


	public Room getRoomConfiguration(String room) {
		return roomConfiguration.get(room);
	}


	public Collection<Room> getAllRoomConfigurations() {
		return roomConfiguration.values();
	}


	public Map<String, Room> getAllRoomConfigurationsMap() {
		return roomConfiguration;
	}


	private String getIpAdress() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

		// Convert little-endian to big-endianif needed
		if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
			ipAddress = Integer.reverseBytes(ipAddress);
		}

		byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

		String ipAddressString;
		try {
			ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
		} catch (UnknownHostException ex) {
			Log.e("WIFIIP", "Unable to get host address.");
			ipAddressString = null;
		}

		if (ipAddressString == null) {
			Log.d(LOG, "IP Adress is 0. Asuming we are in emulated envirenment. using 127.0.0.1");
			ipAddressString = "127.0.0.1";
		}
		Log.d(LOG, "IP Adress for callback is: " + ipAddressString);

		return ipAddressString;
	}


	private boolean isConnectedToWifi(Context context) {

		if (isRunningInEmulator())
			return true;

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
	}


	private boolean isRunningInEmulator() {
		boolean inEmulator = false;
		String brand = Build.BRAND;
		if (brand.compareTo("generic_x86") == 0) {
			inEmulator = true;
		}
		return inEmulator;
	}
}
