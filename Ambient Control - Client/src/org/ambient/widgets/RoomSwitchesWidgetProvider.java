package org.ambient.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class RoomSwitchesWidgetProvider extends AppWidgetProvider {

	private static final String LOG = "widgetProvider";


//	@Override
//	public void onEnabled(Context context) {
//		Log.w(LOG, "onenabled method called");
//		Intent intentToService = new Intent(context, UpdateWidgetService.class);
//		intentToService.setAction(null);
//
//		// Update the widgets via the service
//		context.startService(intentToService);
//	}


//	@Override
//	public void onDeleted(Context context, int[] appWidgetIds) {
//		Log.w(LOG, "onDeleted method called");
//		Intent intentToService = new Intent(context, UpdateWidgetService.class);
//		intentToService.setAction(null);
//		// Update the widgets via the service
//		AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
//		context.stopService(intentToService);
//	}


	@Override
	public void onDisabled(Context context) {
		Log.w(LOG, "onDisabled method called");
		Intent intentToService = new Intent(context, UpdateWidgetService.class);
		intentToService.setAction(null);
		// Update the widgets via the service
		context.stopService(intentToService);
	}


	@Override
	public void onReceive(Context context, Intent intent) {
		Log.w(LOG, "onReceive method called with " + intent.getAction());
		
		//Just handle pending intents from Service
		if (intent.getAction().contains("SWITCH") == true) {
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
		// Get all ids
		ComponentName thisWidget = new ComponentName(context, RoomSwitchesWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// Build the intent to call the service
		Intent intent = new Intent(context, UpdateWidgetService.class);
		intent.setAction("updateWidget");
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

		// Update the widgets via the service
		context.startService(intent);
	}

}
