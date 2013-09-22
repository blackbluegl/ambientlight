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
import java.util.Map;

import org.ambient.control.rest.RestClient;
import org.ambient.control.rest.URLUtils;
import org.ambient.roomservice.socketcallback.CallbackSocketServerRunnable;
import org.ambientlight.room.RoomConfiguration;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


//TODO implement rpc interface to respond directly to client requests and do not wait for anymore for long network requests
/**
 * @author Florian Bornkessel
 * 
 */
public class RoomConfigService extends Service {

	public static final String BROADCAST_INTENT_UPDATE_ROOMCONFIG = "org.ambientcontrol.callback.updateRoomConfig";

	public static final String EXTRA_ROOMCONFIG = "roomConfiguration";
	public static final String EXTRA_SERVERNAME = "serverName";

	private static final String LOG = "org.ambientcontrol.roomConfigService";

	private Map<String, RoomConfiguration> roomConfiguration = new HashMap<String, RoomConfiguration>();
	private Map<String, String> roomNameServerMapping = new HashMap<String, String>();

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

			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				Log.i(LOG, " startCallBackServer and reload roomConfigurations because of ACTION_SCREEN_ON");
				startCallBackServer();
				initAllRoomConfigurations();
			}

			if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				Log.i(LOG, " stopCallBackServer because of ACTION_SCREEN_OFF");
				stopCallBackServer();
			}
		}
	};


	// update request from server
	public synchronized void updateRoomConfigFor(String roomName) {
		try {
			// todo extract servername
			String serverName = roomNameServerMapping.get(roomName);
			RoomConfiguration roomConfig = RestClient.getRoom(serverName);
			// update Model
			roomConfiguration.put(serverName, roomConfig);

			// send BroadcastMessage to listeners who might be interested
			Intent intent = new Intent();
			intent.setAction(BROADCAST_INTENT_UPDATE_ROOMCONFIG);
			intent.putExtra(EXTRA_ROOMCONFIG, roomConfig);
			intent.putExtra(EXTRA_SERVERNAME, serverName);
			sendBroadcast(intent);

		} catch (Exception e) {
			e.printStackTrace();
		}
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
		startCallBackServer();
		// register filter for sytem actions that will call us later
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(receiver, filter);

		// initially load all RoomConfigurations
		initAllRoomConfigurations();
	}


	/**
	 * 
	 */
	private void initAllRoomConfigurations() {
		for (String currentServer : URLUtils.ANDROID_ADT_SERVERS) {
			try {
				RoomConfiguration config = RestClient.getRoom(currentServer);
				roomConfiguration.put(currentServer, config);
				roomNameServerMapping.put(config.roomName, currentServer);
			} catch (Exception e) {
				Log.e(LOG, "error initializing room", e);
			}
		}
	}


	@Override
	public void onDestroy() {
		Log.i(LOG, "onDestroy Called");
		unregisterReceiver(receiver);
		stopCallBackServer();
		super.onDestroy();
	}


	/**
	 * 
	 */
	private synchronized void startCallBackServer() {
		// start socketServer
		if (callbackSocketServer == null) {
			callbackSocketServer = new CallbackSocketServerRunnable(this);
			new Thread(callbackSocketServer).start();
			String hostname = getIpAdress() + ":4321";
			RestClient rest = new RestClient();
			for (String currentServer : URLUtils.ANDROID_ADT_SERVERS) {
				rest.registerCallback(currentServer, hostname);
			}
		}
	}


	/**
	 * 
	 */
	private synchronized void stopCallBackServer() {
		if (callbackSocketServer != null) {
			try {
				String hostname = getIpAdress() + ":4321";
				RestClient rest = new RestClient();
				for (String currentServer : URLUtils.ANDROID_ADT_SERVERS) {
					rest.unregisterCallback(currentServer, hostname);
				}
				callbackSocketServer.stop();
			} catch (IOException e) {
				Log.e(LOG, "error closing callbackSocketService");
			}
			callbackSocketServer = null;
		}
	}


	public RoomConfiguration getRoomConfiguration(String server) {
		return roomConfiguration.get(server);
	}


	public Collection<RoomConfiguration> getAllRoomConfigurations() {
		return roomConfiguration.values();
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
}
