package org.ambient.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class RoomSwitchesWidgetProvider extends AppWidgetProvider {

	private static final String LOG = "RoomSwitchesWidgetProvider";
	public static final String INTENT_UPDATE_VIEW = "org.ambientcontrol.widget.roomswitch.update";


	@Override
	public void onDisabled(Context context) {
		Log.w(LOG, "onDisabled method called. Stopping UpdateWidgetService!");

		Intent intentToService = new Intent(context, UpdateWidgetService.class);
		intentToService.setAction(null);
		// Update the widgets via the service
		context.stopService(intentToService);
	}


	@Override
	public void onReceive(Context context, Intent intent) {
		Log.w(LOG, "onReceive method called with " + intent.getAction());

		// Just handle pending intents from GUI
		if (intent.getAction().contains(UpdateWidgetService.INTENT_SWITCH_CLICKED)) {
			Intent intentToService = new Intent(context, UpdateWidgetService.class);
			intentToService.putExtras(intent.getExtras());
			intentToService.setAction(intent.getAction());
			context.startService(intentToService);
		}

		super.onReceive(context, intent);
	}


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		Log.w(LOG, "onUpdate method called");

		// Build the intent to call the service
		Intent intent = new Intent(context, UpdateWidgetService.class);
		intent.setAction(INTENT_UPDATE_VIEW);

		// Update the widgets via the service
		context.startService(intent);
	}
}
