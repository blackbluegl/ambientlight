package org.ambient.widgets;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.ambient.control.R;
import org.ambient.control.rest.RestClient;
import org.ambient.control.rest.URLUtils;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;


public class UpdateWidgetService extends Service {

	private static final String LOG = "de.vogella.android.widget.example";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		
		String switchRoom = intent.getExtras().getString("switchClicked");
		boolean powerState = intent.getExtras().getBoolean("powerState", false);
		if (switchRoom != null) {
			try {
				RestClient.setPowerStateForRoom(switchRoom, powerState);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
		
		
		Log.i(LOG, "Called");
		String[] serverNames = URLUtils.ANDROID_ADT_SERVERS;
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

		int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		for (int widgetId : allWidgetIds) {

			RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),
					R.layout.widget_roomscenery_switch);
			remoteViews.removeAllViews(R.id.layout_widget_roomswitches);
			for (String room : serverNames) {
				try {
					RoomConfiguration roomConfig = RestClient.getRoom(room, this.getApplicationContext());
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
					
					clickIntent.setAction("Test");
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
		return START_STICKY;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}