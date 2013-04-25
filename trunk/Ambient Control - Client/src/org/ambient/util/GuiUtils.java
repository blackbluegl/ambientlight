package org.ambient.util;

import org.ambient.control.R;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class GuiUtils {
	public static void disableEnableControls(boolean enable, ViewGroup vg){
	    for (int i = 0; i < vg.getChildCount(); i++){
	       View child = vg.getChildAt(i);
	       child.setEnabled(enable);
	       if (child instanceof ViewGroup){ 
	          disableEnableControls(enable, (ViewGroup)child);
	       }
	    }
	}

	public static void toastCause(Exception e, Context ct){
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(ct, (CharSequence) e.getCause(), duration);
		toast.show();
	}
	
	public static boolean isLargeLayout(Activity activity){
		return activity.getResources().getBoolean(R.bool.large_layout);
	}
	
}
