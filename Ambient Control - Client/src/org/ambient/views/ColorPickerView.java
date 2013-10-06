/*  Copyright 2013 Florian Bornkessel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.ambient.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * contributed by Yougli's Box
 * http://www.yougli.net/android/a-photoshop-like-color-picker-for
 * -your-android-application/ "Voilà, this was my first, poor contribution to
 * the wonderful world of Android. I hope this will help some of you, feel free
 * to use, modify, distribute this code, and of course leave your comments there
 * :)"
 * 
 */
public class ColorPickerView extends View {

	public interface OnColorChangedListener {

		void colorChanged( int color);
	}

	private Paint mPaint;
	private float mCurrentHue = 0;
	private int mCurrentX = 0, mCurrentY = 0;
	private int mCurrentColor;
	private final int[] mHueBarColors = new int[258];
	private final int[] mMainColors = new int[65536];
	private OnColorChangedListener mListener;


	public ColorPickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(null, 1);
	}


	public ColorPickerView(Context c, OnColorChangedListener l, int color) {
		super(c);
		init(l, color);
	}


	/**
	 * @param l
	 * @param color
	 * @param defaultColor
	 */
	private void init(OnColorChangedListener l, int color) {
		mListener = l;

		// Get the current hue from the current color and update the main color
		// field
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		mCurrentHue = hsv[0];
		updateMainColors();

		mCurrentColor = color;

		// Initialize the colors of the hue slider bar
		int index = 0;
		for (float i = 0; i < 256; i += 256 / 42) // Red (#f00) to pink (#f0f)
		{
			mHueBarColors[index] = Color.rgb(255, 0, (int) i);
			index++;
		}
		for (float i = 0; i < 256; i += 256 / 42) // Pink (#f0f) to blue (#00f)
		{
			mHueBarColors[index] = Color.rgb(255 - (int) i, 0, 255);
			index++;
		}
		for (float i = 0; i < 256; i += 256 / 42) // Blue (#00f) to light blue
			// (#0ff)
		{
			mHueBarColors[index] = Color.rgb(0, (int) i, 255);
			index++;
		}
		for (float i = 0; i < 256; i += 256 / 42) // Light blue (#0ff) to green
			// (#0f0)
		{
			mHueBarColors[index] = Color.rgb(0, 255, 255 - (int) i);
			index++;
		}
		for (float i = 0; i < 256; i += 256 / 42) // Green (#0f0) to yellow
			// (#ff0)
		{
			mHueBarColors[index] = Color.rgb((int) i, 255, 0);
			index++;
		}
		for (float i = 0; i < 256; i += 256 / 42) // Yellow (#ff0) to red (#f00)
		{
			mHueBarColors[index] = Color.rgb(255, 255 - (int) i, 0);
			index++;
		}

		// Initializes the Paint that will draw the View
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setTextSize(12);
	}


	// Get the current selected color from the hue bar
	private int getCurrentMainColor() {
		int translatedHue = 255 - (int) (mCurrentHue * 255 / 360);
		int index = 0;
		for (float i = 0; i < 256; i += 256 / 42) {
			if (index == translatedHue)
				return Color.rgb(255, 0, (int) i);
			index++;
		}
		for (float i = 0; i < 256; i += 256 / 42) {
			if (index == translatedHue)
				return Color.rgb(255 - (int) i, 0, 255);
			index++;
		}
		for (float i = 0; i < 256; i += 256 / 42) {
			if (index == translatedHue)
				return Color.rgb(0, (int) i, 255);
			index++;
		}
		for (float i = 0; i < 256; i += 256 / 42) {
			if (index == translatedHue)
				return Color.rgb(0, 255, 255 - (int) i);
			index++;
		}
		for (float i = 0; i < 256; i += 256 / 42) {
			if (index == translatedHue)
				return Color.rgb((int) i, 255, 0);
			index++;
		}
		for (float i = 0; i < 256; i += 256 / 42) {
			if (index == translatedHue)
				return Color.rgb(255, 255 - (int) i, 0);
			index++;
		}
		return Color.RED;
	}


	// Update the main field colors depending on the current selected hue
	private void updateMainColors() {
		int mainColor = getCurrentMainColor();
		int index = 0;
		int[] topColors = new int[256];
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 256; x++) {
				if (y == 0) {
					mMainColors[index] = Color.rgb(255 - (255 - Color.red(mainColor)) * x / 255,
							255 - (255 - Color.green(mainColor)) * x / 255, 255 - (255 - Color.blue(mainColor)) * x / 255);
					topColors[x] = mMainColors[index];
				} else {
					mMainColors[index] = Color.rgb((255 - y) * Color.red(topColors[x]) / 255,
							(255 - y) * Color.green(topColors[x]) / 255, (255 - y) * Color.blue(topColors[x]) / 255);
				}
				index++;
			}
		}
	}


	@Override
	protected void onDraw(Canvas canvas) {
		int translatedHue = 255 - (int) (mCurrentHue * 255 / 360);
		// Display all the colors of the hue bar with lines
		for (int x = 0; x < 256; x++) {
			// If this is not the current selected hue, display the actual color
			if (translatedHue != x) {
				mPaint.setColor(mHueBarColors[x]);
				mPaint.setStrokeWidth(1);
			} else // else display a slightly larger black line
			{
				mPaint.setColor(Color.BLACK);
				mPaint.setStrokeWidth(3);
			}
			canvas.drawLine(x + 10, 0, x + 10, 40, mPaint);
		}

		// Display the main field colors using LinearGradient
		for (int x = 0; x < 256; x++) {
			int[] colors = new int[2];
			colors[0] = mMainColors[x];
			colors[1] = Color.BLACK;
			Shader shader = new LinearGradient(0, 50, 0, 306, colors, null, Shader.TileMode.REPEAT);
			mPaint.setShader(shader);
			canvas.drawLine(x + 10, 50, x + 10, 306, mPaint);
		}
		mPaint.setShader(null);

		// Display the circle around the currently selected color in the main
		// field
		if (mCurrentX != 0 && mCurrentY != 0) {
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setColor(Color.BLACK);
			canvas.drawCircle(mCurrentX, mCurrentY, 10, mPaint);
		}
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(276, 366);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_DOWN)
			return true;
		float x = event.getX();
		float y = event.getY();

		// If the touch event is located in the hue bar
		if (x > 10 && x < 266 && y > 0 && y < 40) {
			// Update the main field colors
			mCurrentHue = (255 - x) * 360 / 255;
			updateMainColors();

			// Update the current selected color
			int transX = mCurrentX - 10;
			int transY = mCurrentY - 60;
			int index = 256 * (transY - 1) + transX;
			if (index > 0 && index < mMainColors.length) {
				mCurrentColor = mMainColors[256 * (transY - 1) + transX];
			}
			mListener.colorChanged(mCurrentColor);
			// Force the redraw of the dialog
			invalidate();
		}

		// If the touch event is located in the main field
		if (x > 10 && x < 266 && y > 50 && y < 306) {
			mCurrentX = (int) x;
			mCurrentY = (int) y;
			int transX = mCurrentX - 10;
			int transY = mCurrentY - 60;
			int index = 256 * (transY - 1) + transX;
			if (index > 0 && index < mMainColors.length) {
				// Update the current color
				mCurrentColor = mMainColors[index];
				mListener.colorChanged(mCurrentColor);
				// Force the redraw of the dialog
				invalidate();
			}
		}

		return true;
	}
}
