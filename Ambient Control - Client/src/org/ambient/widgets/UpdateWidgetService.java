package org.ambient.widgets;

import org.ambient.control.R;
import org.ambient.control.rest.RestClient;
import org.ambient.control.rest.URLUtils;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;


public class UpdateWidgetService extends Service {

	private static final String LOG = "UpdateWidgetService";

	/**
	 * used for receiving USER_PRESENT and wifi Events and update the icon after
	 * user unlocks the screen.
	 */
	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		private static final String LOG = "UpdateWidgetService.BroadCastReceiver";


		private void updateWidget(Context context, Intent intent, int[] allWidgetIds) {
			Intent updateIntent = new Intent(context, UpdateWidgetService.class);
			updateIntent.setAction("updateWidget");
			updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
			context.startService(updateIntent);
		}


		private void disableWidget(Context context, Intent intent, int[] allWidgetIds) {
			Intent updateIntent = new Intent(context, UpdateWidgetService.class);
			updateIntent.setAction("disableWidget");
			updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
			context.startService(updateIntent);
		}


		@Override
		public void onReceive(Context context, Intent intent) {
//			Log.i(LOG, "Broadcast received.");
//			if (intent != null && intent.getAction() != null) {
//				Log.i(LOG, "Intent was: " + intent.getAction());
//			}

			String action = intent.getAction();

			ComponentName thisWidget = new ComponentName(context, RoomSwitchesWidgetProvider.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
			int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

			if (action.equals(Intent.ACTION_USER_PRESENT)) {
				Log.i(LOG, " updateWidget because of ACTION_USER_PRESENT");
				updateWidget(context, intent, allWidgetIds);
			}

			
//			if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
//				if (isConnectedToWifi(context)) {
//					Log.i(LOG, " updateWidget because of SUPPLICANT_CONNECTION_CHANGE_ACTION and isConnected=true");
//					updateWidget(context, intent, allWidgetIds);
//				}
//				else{
//					Log.i(LOG, " disableWidget because of SUPPLICANT_CONNECTION_CHANGE_ACTION and isConnected=false");
//					disableWidget(context, intent, allWidgetIds);
//				}
//			}
//			
			
			//wlan on, wlan reset
			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				if (isConnectedToWifi(context)) {
					Log.i(LOG, " updateWidget because of NETWORK_STATE_CHANGED_ACTION and isConnected=true");
					updateWidget(context, intent, allWidgetIds);
				}
//				//wlan off,
//				else{
//					Log.i(LOG, " disableWidget because of NETWORK_STATE_CHANGED_ACTION and isConnected=false");
//					disableWidget(context, intent, allWidgetIds);
//				}
			}

			
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				//fm off, wlan off, wlan lost
				if (!isConnectedToWifi(context)) {
					Log.i(LOG, " disable because of CONNECTIVITY_ACTION and isConnected=false");
					disableWidget(context, intent, allWidgetIds);
				}
//				//fm on, wlan on, wlan reset (several times)
//				else{
//					Log.i(LOG, " updateWidget because of CONNECTIVITY_ACTION and isConnected=true");
//					updateWidget(context, intent, allWidgetIds);
//				}
			}

//			if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
//				SupplicantState supState;
//				WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//				supState = wifiInfo.getSupplicantState();
//				//wlan reset(two times)
//				if (supState.equals(SupplicantState.COMPLETED)) {
//					Log.i(LOG, " updateWidget because of SUPPLICANT_STATE_CHANGED_ACTION and SupplicantState.COMPLETED");
//					Intent updateIntent = new Intent(context, UpdateWidgetService.class);
//					updateIntent.setAction("updateWidget");
//					updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
//					context.startService(updateIntent);
//					// determine if this is ever called
//				} else if (supState.equals(SupplicantState.DISCONNECTED)) {
//					Log.i(LOG, " disable because of SUPPLICANT_STATE_CHANGED_ACTION and SupplicantState.DISCONNECTED");
//					Intent updateIntent = new Intent(context, UpdateWidgetService.class);
//					updateIntent.setAction("disableWidget");
//					updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
//					context.startService(updateIntent);
//				}
//			}
		}
	};


	@Override
	public void onCreate() {
		Log.i(LOG, "onCreated Called");
		// TODO remove unused filters and privileges
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_USER_PRESENT);
//		filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
//		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(receiver, filter);
	}


	@Override
	public void onDestroy() {
		Log.i(LOG, "onDestroy Called");
		unregisterReceiver(receiver);
		super.onDestroy();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

		if (intent == null || intent.getAction() == null) {
			Log.i(LOG, "onStartCommand: no intent sent. do not do anything.");
			return START_STICKY;
		}
		if (intent.getAction() != null) {
			Log.i(LOG, "Called onStartCommand with action: " + intent.getAction());
		} else {
			Log.i(LOG, "Called onStartCommand with intent but without action");
		}

		if (intent.getAction().equals("disableWidget")) {
			setWidgetToDisabledView(intent, appWidgetManager);
			return START_STICKY;
		}

		// TODO check if this stetement is now obsolete
		if (this.isConnectedToWifi(getApplicationContext()) == false) {
			Log.i(LOG, "onStartCommand: No wifi available. Disableing the widget. This should not be nescessary");
			setWidgetToDisabledView(intent, appWidgetManager);
			return START_STICKY;
		}

		if (intent.getAction().contains("SWITCH")) {
			switchRoom(intent, appWidgetManager);
			return START_STICKY;
		}

		if (intent.getAction().equals("updateWidget")) {
			updateWidget(intent, appWidgetManager);
			return START_STICKY;
		}

		return START_STICKY;
	}


	private void updateWidget(Intent intent, AppWidgetManager appWidgetManager) {
		String[] serverNames = URLUtils.ANDROID_ADT_SERVERS;

		int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		for (int widgetId : allWidgetIds) {

			RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),
					R.layout.widget_roomscenery_switch);
			remoteViews.removeAllViews(R.id.layout_widget_roomswitches);

			for (String room : serverNames) {
				try {
					RoomConfiguration roomConfig = RestClient.getRoom(room);
					if (roomConfig == null) {
						continue;
					}
					boolean switchOn = false;
					for (RoomItemConfiguration current : roomConfig.roomItemConfigurations) {
						if (current.getSceneryConfigurationBySceneryName(roomConfig.currentScenery).powerState == true) {
							switchOn = true;
							break;
						}
					}
					RemoteViews switchIcon = new RemoteViews(this.getApplicationContext().getPackageName(),
							R.layout.widget_icon_switch);

					if (switchOn) {
						switchIcon.setImageViewResource(R.id.iconWidgetSwitch, R.drawable.ic_power_active);
					} else {
						switchIcon.setImageViewResource(R.id.iconWidgetSwitch, R.drawable.ic_power_disabled);
					}
					switchIcon.setTextViewText(R.id.textViewWidgetSwitch, roomConfig.roomName);
					remoteViews.addView(R.id.layout_widget_roomswitches, switchIcon);

					// Register an onClickListener
					Intent clickIntent = new Intent(this.getApplicationContext(), RoomSwitchesWidgetProvider.class);

					clickIntent.setAction("SWITCH " + room);
					clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
					clickIntent.putExtra("switchClicked", room);
					clickIntent.putExtra("powerState", !switchOn);

					PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
							PendingIntent.FLAG_UPDATE_CURRENT);
					switchIcon.setOnClickPendingIntent(R.id.iconWidgetSwitch, pendingIntent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}


	private void switchRoom(Intent intent, AppWidgetManager appWidgetManager) {
		String switchRoom = intent.getExtras().getString("switchClicked");
		boolean powerState = intent.getExtras().getBoolean("powerState", false);

		setWidgetToRefreshView(intent, appWidgetManager);

		try {
			RestClient.setPowerStateForRoom(switchRoom, powerState);
		} catch (Exception e) {
			e.printStackTrace();
		}

		updateWidget(intent, appWidgetManager);
	}


	private void setWidgetToRefreshView(Intent intent, AppWidgetManager appWidgetManager) {
		int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		for (int widgetId : allWidgetIds) {

			RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),
					R.layout.widget_roomscenery_switch);
			remoteViews.removeAllViews(R.id.layout_widget_roomswitches);
			RemoteViews switchIcon = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_icon_switch);
			switchIcon.setImageViewResource(R.id.iconWidgetSwitch, R.drawable.ic_action_refresh);
			switchIcon.setTextViewText(R.id.textViewWidgetSwitch, "please Wait");
			remoteViews.addView(R.id.layout_widget_roomswitches, switchIcon);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}


	private void setWidgetToDisabledView(Intent intent, AppWidgetManager appWidgetManager) {
		int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

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