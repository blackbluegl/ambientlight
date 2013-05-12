package org.ambient.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class RoomSwitchesWidgetProvider extends AppWidgetProvider {
	  private static final String LOG = "de.vogella.android.widget.example";

	  @Override
	  public void onReceive(Context context, Intent intent){
		  
		  Intent intentToService = new Intent(context,
			        UpdateWidgetService.class);
			    intentToService.putExtras(intent.getExtras());

			    // Update the widgets via the service
			    context.startService(intentToService);
		  super.onReceive(context, intent);
	  }
	  
	  @Override
	  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
	      int[] appWidgetIds) {
		 
	    Log.w(LOG, "onUpdate method called");
	    // Get all ids
	    ComponentName thisWidget = new ComponentName(context,
	        RoomSwitchesWidgetProvider.class);
	    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

	    // Build the intent to call the service
	    Intent intent = new Intent(context,
	        UpdateWidgetService.class);
	    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

	    // Update the widgets via the service
	    context.startService(intent);
	  }
	
}