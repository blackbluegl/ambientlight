package org.ambient.widgets;

import java.util.HashMap;
import java.util.Map;

import org.ambient.control.R;
import org.ambient.control.rest.RestClient;
import org.ambient.control.rest.URLUtils;
import org.ambient.roomservice.RoomConfigService;
import org.ambientlight.process.events.SwitchEventConfiguration;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.actors.ActorConfiguration;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;


public class UpdateWidgetService extends Service {

	public static final String INTENT_SWITCH_CLICKED = "org.ambientcontrol.widget.roomswitch.clicked";

	private static final String LOG = "UpdateWidgetService";

	RoomConfigService roomService = null;

	private ServiceConnection roomServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.i(LOG, "roomService connected");

			roomService = ((RoomConfigService.MyBinder) binder).getService();
			updateWidget();
		}


		@Override
		public void onServiceDisconnected(ComponentName className) {
			Log.i(LOG, "roomService disconnected. setting Widget to disabled");

			roomService = null;
			setWidgetToDisabledView();
		}
	};


	/**
	 * used to receive external events from wifi and user
	 */
	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		private static final String LOG = "UpdateWidgetService.BroadCastReceiver";


		private boolean isRunningInEmulator() {
			boolean inEmulator = false;
			String brand = Build.BRAND;
			if (brand.compareTo("generic_x86") == 0) {
				inEmulator = true;
			}
			return inEmulator;
		}


		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			// the roomconfig was changed from elsewhere
			if (action.equals(RoomConfigService.BROADCAST_INTENT_UPDATE_ROOMCONFIG)) {
				Log.i(LOG, "updateWidget because of BROADCAST_INTENT_UPDATE_ROOMCONFIG");
				updateWidget();
			}

			// user is present
			if (action.equals(Intent.ACTION_USER_PRESENT)) {
				Log.i(LOG, "updateWidget because of ACTION_USER_PRESENT");
				updateWidget();
			}

			// wlan on, wlan reset,fm on
			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) && isConnectedToWifi(context)) {
				Log.i(LOG, "updateWidget because of NETWORK_STATE_CHANGED_ACTION and isConnected=true");
				updateWidget();
			}

			// fm off, wlan off, wlan lost
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) && !isConnectedToWifi(context) && !isRunningInEmulator()) {
				Log.i(LOG, " disable because of CONNECTIVITY_ACTION and isConnected=false");
				setWidgetToDisabledView();
			}
		}
	};


	@Override
	public void onCreate() {
		Log.i(LOG, "onCreated Called");
		bindService(new Intent(this, RoomConfigService.class), roomServiceConnection, Context.BIND_AUTO_CREATE);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_USER_PRESENT);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(RoomConfigService.BROADCAST_INTENT_UPDATE_ROOMCONFIG);
		registerReceiver(receiver, filter);
	}


	@Override
	public void onDestroy() {
		Log.i(LOG, "onDestroy Called");
		unregisterReceiver(receiver);
		unbindService(roomServiceConnection);
		super.onDestroy();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

		if (intent == null || intent.getAction() == null) {
			Log.i(LOG, "onStartCommand: no intent sent. do not do anything.");
			return START_STICKY;
		}

		// //disable widget
		// if (intent.getAction().equals("disableWidget")) {
		// setWidgetToDisabledView();
		// return START_STICKY;
		// }

		// if there is no connection set widget to disabled view
		if (this.isConnectedToWifi(getApplicationContext()) == false && Build.BRAND.startsWith("generic") == false) {
			Log.i(LOG, "onStartCommand: No wifi available. Disableing the widget. This should not be nescessary");
			setWidgetToDisabledView();
			return START_STICKY;
		}

		// event handling
		if (intent.getAction().contains(INTENT_SWITCH_CLICKED)) {
			switchRoom(intent, appWidgetManager);
			return START_STICKY;
		}

		// update from widgetProvider
		if (intent.getAction().equals(RoomSwitchesWidgetProvider.INTENT_UPDATE_VIEW)) {
			updateWidget();
			return START_STICKY;
		}

		return START_STICKY;
	}


	private void updateWidget() {
		if (this.roomService == null)
			return;

		ComponentName thisWidget = new ComponentName(this, RoomSwitchesWidgetProvider.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		Map<String, RoomConfiguration> config = new HashMap<String, RoomConfiguration>();

		for (String currentServer : URLUtils.ANDROID_ADT_SERVERS) {
			RoomConfiguration roomConfig = roomService.getRoomConfiguration(currentServer);
			if (roomConfig == null) {
				continue;
			}
			config.put(currentServer, roomConfig);
		}

		String[] serverNames = URLUtils.ANDROID_ADT_SERVERS;

		for (int widgetId : allWidgetIds) {

			RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),
					R.layout.widget_roomscenery_switch);
			remoteViews.removeAllViews(R.id.layout_widget_roomswitches);

			for (String room : serverNames) {
				RoomConfiguration roomConfig = config.get(room);
				boolean switchOn = false;
				for (ActorConfiguration current : roomConfig.actorConfigurations.values()) {
					if (current.getPowerState() == true) {
						switchOn = true;
						break;
					}
				}
				RemoteViews switchIcon = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_icon_switch);

				if (switchOn) {
					switchIcon.setImageViewResource(R.id.iconWidgetSwitch, R.drawable.ic_power_active);
				} else {
					switchIcon.setImageViewResource(R.id.iconWidgetSwitch, R.drawable.ic_power_disabled);
				}
				switchIcon.setTextViewText(R.id.textViewWidgetSwitch, roomConfig.roomName);
				remoteViews.addView(R.id.layout_widget_roomswitches, switchIcon);

				// Register an onClickListener
				Intent clickIntent = new Intent(getApplicationContext(), RoomSwitchesWidgetProvider.class);

				clickIntent.setAction(INTENT_SWITCH_CLICKED + "." + room);
				clickIntent.putExtra("switchClicked", room);
				clickIntent.putExtra("powerState", !switchOn);

				PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				switchIcon.setOnClickPendingIntent(R.id.iconWidgetSwitch, pendingIntent);
			}
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}


	private void switchRoom(Intent intent, AppWidgetManager appWidgetManager) {
		String switchRoom = intent.getExtras().getString("switchClicked");
		boolean powerState = intent.getExtras().getBoolean("powerState", false);

		setWidgetToRefreshView();

		try {

			RestClient rest = new RestClient();
			SwitchEventConfiguration event = new SwitchEventConfiguration();
			event.eventGeneratorName = "RoomSwitch";
			event.powerState = powerState;
			rest.sendEvent(switchRoom, event);
		} catch (Exception e) {
			Log.e(LOG,
					"error while trying to switch room. maybe the server is down or we are in the wrong wifi net. disableing the widget till next wakeup.",
					e);
			setWidgetToDisabledView();
		}
	}


	private void setWidgetToRefreshView() {

		ComponentName thisWidget = new ComponentName(this, RoomSwitchesWidgetProvider.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		for (int widgetId : allWidgetIds) {

			RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),
					R.layout.widget_roomscenery_switch);
			remoteViews.removeAllViews(R.id.layout_widget_roomswitches);
			RemoteViews switchIcon = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_icon_switch);
			switchIcon.setImageViewResource(R.id.iconWidgetSwitch, R.drawable.ic_action_refresh);
			switchIcon.setTextViewText(R.id.textViewWidgetSwitch, "");
			remoteViews.addView(R.id.layout_widget_roomswitches, switchIcon);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}


	private void setWidgetToDisabledView() {

		ComponentName thisWidget = new ComponentName(this, RoomSwitchesWidgetProvider.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		for (int widgetId : allWidgetIds) {

			RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),
					R.layout.widget_roomscenery_switch);
			remoteViews.removeAllViews(R.id.layout_widget_roomswitches);
			RemoteViews switchIcon = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_icon_switch);
			switchIcon.setImageViewResource(R.id.iconWidgetSwitch, R.drawable.ic_power_disabled);
			switchIcon.setTextViewText(R.id.textViewWidgetSwitch, "");
			remoteViews.addView(R.id.layout_widget_roomswitches, switchIcon);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}


	private boolean isConnectedToWifi(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}