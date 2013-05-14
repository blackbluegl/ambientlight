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

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG, "onReceived Called");
			String action = intent.getAction();
			ComponentName thisWidget = new ComponentName(context, RoomSwitchesWidgetProvider.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
			int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

			if (action.equals(Intent.ACTION_USER_PRESENT)) {
				Intent updateIntent = new Intent(context, UpdateWidgetService.class);
				updateIntent.setAction("updateWidget");
				updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
				context.startService(updateIntent);
			}

			if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
				if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
					Intent updateIntent = new Intent(context, UpdateWidgetService.class);
					updateIntent.setAction("updateWidget");
					updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
					context.startService(updateIntent);
				} else {
					Intent updateIntent = new Intent(context, UpdateWidgetService.class);
					updateIntent.setAction("disableWidget");
					updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
					context.startService(updateIntent);
				}
			}
		}
	};


	@Override
	public void onCreate() {
		Log.i(LOG, "onCreated Called");
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_USER_PRESENT);
		filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
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
		Log.i(LOG, "Called onStartCommand"); 
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

		if(intent == null|| intent.getAction()==null){
			Log.i(LOG, "onStartCommand: no intent sent. do not do anything.");
			return START_STICKY;
		}
		
		if(this.isConnectedToWifi(getApplicationContext())== false){
			Log.i(LOG, "onStartCommand: No wifi available. Disableing the widget.");
			setWidgetToDisabledView(intent, appWidgetManager);
			return START_STICKY;
		}
		
		if (intent.getAction().contains("SWITCH")) {
			switchRoom(intent, appWidgetManager);
		}

		if (intent.getAction().equals("updateWidget")) {
			updateWidget(intent, appWidgetManager);
		}
		if (intent.getAction().equals("disableWidget")) {
			setWidgetToDisabledView(intent, appWidgetManager);
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