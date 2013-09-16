package org.ambient.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;


public class WidgetUtils {

	/**
	 * sends an update event to all widgets. so they can be in sync with the app status.
	 */
	public static void notifyWidgets(Activity activity) {
		int ids[] = AppWidgetManager.getInstance(activity.getApplication()).getAppWidgetIds(
				new ComponentName(activity.getApplication(), RoomSwitchesWidgetProvider.class));
		Intent intent = new Intent(activity, RoomSwitchesWidgetProvider.class);
		intent.setAction(RoomSwitchesWidgetProvider.ACTION_UPDATE_FROM_ACTIVITY);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
		activity.sendBroadcast(intent);
	}
}
