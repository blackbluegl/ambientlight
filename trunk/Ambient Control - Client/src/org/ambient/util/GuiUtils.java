package org.ambient.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.ambient.control.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class GuiUtils {

	public static void disableEnableControls(boolean enable, ViewGroup vg) {
		for (int i = 0; i < vg.getChildCount(); i++) {
			View child = vg.getChildAt(i);
			child.setEnabled(enable);
			if (child instanceof ViewGroup) {
				disableEnableControls(enable, (ViewGroup) child);
			}
		}
	}


	public static void toastCause(Exception e, Context ct) {
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(ct, (CharSequence) e.getCause(), duration);
		toast.show();
	}


	public static boolean isLargeLayout(Activity activity) {
		return activity.getResources().getBoolean(R.bool.large_layout);
	}


	/**
	 * thanks to doughw http://stackoverflow.com/questions/3495890/how-can-i-put-
	 * a-listview-into-a-scrollview-without-it-collapsing/3495908#3495908
	 * 
	 * @param listView
	 */
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null)
			// pre-condition
			return;

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}


	public static Object deepCloneSerializeable(Object input) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(input);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}


	/**
	 * @param temp
	 * @return
	 */
	public static int getTemperatureTextColor(float temp, float comfortTemp, float maxTemp, float minTemp) {
		// create color
		int currentBarColor = 0;
		// warm colors above comfort temp
		if (temp >= comfortTemp) {
			int redPart = (int) ((temp - comfortTemp) / (maxTemp - comfortTemp) * 255);
			int greenPart = 255 - redPart;
			int bluePart = 0;
			currentBarColor = Color.rgb(redPart, greenPart, bluePart);
		}
		// cold colors below comfort temp
		else {
			int redPart = 0;
			int greenPart = (int) ((temp - minTemp) / (comfortTemp - minTemp) * 255);
			int bluePart = 255 - greenPart;
			currentBarColor = Color.rgb(redPart, greenPart, bluePart);
		}
		return currentBarColor;
	}


	/**
	 * return color based on the value and multiplied with a factor
	 * 
	 * @param factor
	 * @param color
	 * @return color as int value multiplied with a factor
	 */
	public static int getColor(float factor, int color) {
		int rNew = (int) (Color.red(color) * factor > 255 ? 255 : Color.red(color) * factor);
		int gNew = (int) (Color.green(color) * factor > 255 ? 255 : Color.green(color) * factor);
		int bNew = (int) (Color.blue(color) * factor > 255 ? 255 : Color.blue(color) * factor);
		return Color.rgb(rNew, gNew, bNew);
	}

}
