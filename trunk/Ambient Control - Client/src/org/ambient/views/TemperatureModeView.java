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

import org.ambient.util.GuiUtils;
import org.ambientlight.room.entities.climate.util.MaxThermostateMode;
import org.ambientlight.room.entities.climate.util.MaxUtil;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


/**
 * widget to control the temperature of a room. supports three modes A,M,T and boostMode.
 * 
 * @author Florian Bornkessel
 * 
 */
public class TemperatureModeView extends View {

	public interface TemperatureChangeListener {

		public void onTemperatureChange(float temperature);
	}

	public interface ModeChangeListener {

		public void onModeChanged(MaxThermostateMode mode);
	}

	public TemperatureChangeListener temperatureListener = null;

	public ModeChangeListener modeChangeListener = null;

	public int boostDurationInSeconds = 300;

	public float minTemp = MaxUtil.MIN_TEMPERATURE;

	public float maxTemp = MaxUtil.MAX_TEMPERATURE;

	public float comfortTemp = 21.5f;

	float currentTemp = 28.5f;

	boolean boostMode = false;
	int boostSecondsToRun = 0;

	int viewWidth = 0;
	int viewHeight = 0;

	int barOffSetX = 0;
	int barOffSetYTop = 0;
	int barOffsetYBottom = 0;

	Paint bgPaint = new Paint();
	Rect bgRect = new Rect();

	Paint barPaint = new Paint();
	Point[] barPoints;
	Path barPath = new Path();

	Paint markerPaint = new Paint();
	RectF markerRect = new RectF();

	TextPaint valuePaint = new TextPaint();

	String modeTextValue = "A";
	Paint modeTextPaint = new Paint();
	RectF modeBorderRect = new RectF();
	Paint modeBorderPaint = new Paint();

	GestureDetector gestureDetector;

	ValueAnimator boostDurationValueAnimator;


	public TemperatureModeView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// background
		bgPaint.setStyle(Paint.Style.FILL);
		bgPaint.setColor(0x00333333);

		// the value on top for seconds or degrees celsius
		valuePaint.setTextAlign(Align.CENTER);
		valuePaint.setColor(Color.WHITE);
		valuePaint.setAntiAlias(true);
		valuePaint.setShadowLayer(2.0f, 1.0f, 1.0f, Color.BLACK);

		// text for letter in mode on bottom
		Typeface tfText = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
		modeTextPaint.setTypeface(tfText);
		modeTextPaint.setTextAlign(Align.CENTER);
		modeTextPaint.setColor(Color.WHITE);
		modeTextPaint.setAntiAlias(true);
		modeTextPaint.setShadowLayer(2.0f, 1.0f, 1.0f, Color.BLACK);

		// border on mode icon
		modeBorderPaint.setStyle(Paint.Style.STROKE);
		modeBorderPaint.setColor(0xFFFFFFFF);
		modeBorderPaint.setAntiAlias(true);

		// temperature bar
		barPaint.setStyle(Paint.Style.FILL);
		barPaint.setAntiAlias(true);

		// marker over temperature bar
		markerPaint.setStyle(Paint.Style.STROKE);
		markerPaint.setColor(0xBBFFFFFF);
		markerPaint.setAntiAlias(true);

		// register gesture Listener
		gestureDetector = new GestureDetector(TemperatureModeView.this.getContext(), new GestureListener());

		// register boost countdown animator
		boostDurationValueAnimator = ValueAnimator.ofInt(boostDurationInSeconds, 0).setDuration(boostDurationInSeconds * 1000);
		boostDurationValueAnimator.addUpdateListener(new BoostUpdateTimerListener());
	}


	public void setBoostMode(int durationToGoInSeconds) {
		if (durationToGoInSeconds > 0) {
			currentTemp = maxTemp;
			boostMode = true;
			boostDurationValueAnimator.setDuration(durationToGoInSeconds * 1000);
			boostDurationValueAnimator.start();
			modeTextValue = "B";
		} else {
			boostMode = false;
			boostDurationValueAnimator.cancel();
			modeTextValue = "";
		}
		invalidate();
	}


	public MaxThermostateMode getMode() {
		if (modeTextValue.equals("A"))
			return MaxThermostateMode.AUTO;
		else if (modeTextValue.equals("B"))
			return MaxThermostateMode.BOOST;
		else if (modeTextValue.equals("M"))
			return MaxThermostateMode.MANUAL;
		else
			return MaxThermostateMode.TEMPORARY;
	}


	public void setMode(MaxThermostateMode mode) {
		if (mode == MaxThermostateMode.AUTO) {
			modeTextValue = "A";
		} else if (mode == MaxThermostateMode.BOOST) {
			modeTextValue = "B";
		} else if (mode == MaxThermostateMode.MANUAL) {
			modeTextValue = "M";
		} else if (mode == MaxThermostateMode.TEMPORARY) {
			modeTextValue = "T";
		}
	}


	public void setTemp(float temp) {
		currentTemp = temp;
		// if (viewWidth != 0) {
		// invalidate();
		// } else {
		// currentTemp = temp;
		// }
		invalidate();
	}


	public float getTemp() {
		return currentTemp;
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// update bar
		updateTemperatureBar(currentTemp);

		// draw background
		bgRect.set(0, 0, canvas.getWidth(), canvas.getHeight());
		canvas.drawRect(bgRect, bgPaint);

		// draw bar
		barPath.moveTo(0, 0);
		for (Point current : barPoints) {
			barPath.lineTo(current.x, current.y);
		}
		barPath.lineTo(barPoints[0].x, barPoints[0].y);
		canvas.drawPath(barPath, barPaint);

		// draw mode icon border
		canvas.drawRoundRect(modeBorderRect, 6, 6, modeBorderPaint);
		canvas.drawText(modeTextValue, modeBorderRect.centerX(), modeBorderRect.centerY()
				- ((modeTextPaint.descent() + modeTextPaint.ascent()) / 2), modeTextPaint);

		if (boostMode) {
			// draw value text with seconds
			canvas.drawText(String.valueOf(boostSecondsToRun), viewWidth / 2, viewHeight / 8, valuePaint);
		} else {
			// draw value text with degrees celsius
			canvas.drawText(String.format("%.1f", currentTemp) + "°C", viewWidth / 2, viewHeight / 8, valuePaint);

			// draw marker if not in boost mode
			canvas.drawRoundRect(markerRect, 4, 4, markerPaint);
		}
	}


	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);

		// set widgets size
		viewWidth = xNew;
		viewHeight = yNew;

		// calculate offsets for later calculation
		barOffSetX = viewWidth / 10;
		barOffSetYTop = viewHeight / 6;
		barOffsetYBottom = viewHeight / 6;

		// calculate mode icon
		float centerY = viewHeight - (barOffsetYBottom / 2.0f);
		float lengthX = viewWidth - 2 * barOffSetX;
		float lengthY = barOffsetYBottom;
		// to have it square use the smaller side
		float sideLength = 0;
		if (lengthX > lengthY) {
			sideLength = lengthY * 0.8f;
		} else {
			sideLength = lengthX * 0.8f;

		}
		// set calculated values to the mode icon border
		modeBorderRect.left = barOffSetX;
		modeBorderRect.bottom = centerY + sideLength / 2.0f;
		modeBorderRect.right = barOffSetX + sideLength;
		modeBorderRect.top = centerY - sideLength / 2.0f;
		modeBorderPaint.setStrokeWidth((viewHeight + viewWidth) / 128);
		modeTextPaint.setTextSize(sideLength);

		// calculate textSize for value based on height and width
		float valueTextSize = 0;
		if (viewHeight / 3 < viewWidth) {
			valueTextSize = viewHeight / 10;
		} else {
			valueTextSize = viewWidth / 4;
		}
		valuePaint.setTextSize(valueTextSize);

		// calculate marker size
		float markerStrokeWitdh = (viewHeight - barOffsetYBottom - barOffSetYTop) / 70;
		markerPaint.setStrokeWidth(markerStrokeWitdh);
		markerPaint.setShadowLayer(markerStrokeWitdh, markerStrokeWitdh / 2, markerStrokeWitdh / 2, Color.BLACK);

		// calculate shape of the temperature bar - sideLength is used here to have the same width for mode icon and bar
		barPoints = new Point[] { new Point(barOffSetX, barOffSetYTop), new Point(viewWidth - barOffSetX, barOffSetYTop),
				new Point((int) (barOffSetX + sideLength), viewHeight - barOffsetYBottom),
				new Point(barOffSetX, viewHeight - barOffsetYBottom) };

		// update temperature bar but do not call client listener
		this.updateTemperatureBar(currentTemp);
	}


	/**
	 * updates the temperatureBar. Color and the marker level.
	 * 
	 * @param temp
	 */
	private void updateTemperatureBar(float temp) {
		if (temp > maxTemp) {
			temp = maxTemp;
		}
		if (temp < minTemp) {
			temp = minTemp;
		}

		float factor = (temp - minTemp) / (maxTemp - minTemp);
		int rightEnd = (int) (barPoints[2].x + (factor) * (barPoints[1].x - barPoints[2].x));
		markerRect.left = (int) (barOffSetX * 0.9);
		markerRect.right = rightEnd * 1.02f;
		markerRect.bottom = viewHeight - barOffsetYBottom - (factor) * (viewHeight - barOffsetYBottom - barOffSetYTop);
		markerRect.top = markerRect.bottom - viewHeight / 60;

		int currentBarColor = GuiUtils.getTemperatureTextColor(temp, comfortTemp, maxTemp, minTemp);
		// glossy effect for this color
		int[] colors = new int[5];
		colors[0] = GuiUtils.getColor(10f, currentBarColor);
		colors[1] = GuiUtils.getColor(2.5f, currentBarColor);
		colors[2] = GuiUtils.getColor(1.5f, currentBarColor);
		colors[3] = GuiUtils.getColor(0.8f, currentBarColor);
		colors[4] = GuiUtils.getColor(0.2f, currentBarColor);
		// positions in gradient for glossy effect
		float[] colorPositions = new float[] { 0f, 0.3f, 0.5f, 0.501f, 1.0f };

		barPaint.setShader(new LinearGradient(0, 0, 0, viewHeight, colors, colorPositions, Shader.TileMode.MIRROR));
	}


	@Override
	/**
	 * send gestures to gestureListener
	 */
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	/**
	 * handle inputs from user
	 * 
	 * @author Florian Bornkessel
	 * 
	 */
	class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			handleTemperatureChangeEvent(e2);
			invalidate();
			return true;
		}


		@Override
		public boolean onDown(MotionEvent e) {
			// area below the slider
			if (e.getY() > viewHeight - barOffsetYBottom) {
				handleModeChange();
			}
			// area between the mode icon area and the value area on top
			else if (e.getY() >= barOffSetYTop) {
				handleTemperatureChangeEvent(e);
			}

			invalidate();

			return true;
		}


		private void handleTemperatureChangeEvent(MotionEvent e) {
			// slider is not activated while Boost mode
			if (boostMode)
				return;
			// calculate factor between 0 and 1 for the slider
			float factor = (viewHeight - e.getY() - barOffsetYBottom) / (viewHeight - barOffsetYBottom - barOffSetYTop);
			// get temperature
			float temp = (maxTemp - minTemp) * factor + minTemp;

			temp = (float) (Math.round(temp * 10) / 10.0);

			// store temperature
			currentTemp = temp;

			// update listener
			if (temperatureListener != null) {
				temperatureListener.onTemperatureChange(temp);
			}
			invalidate();
		}


		private void handleModeChange() {
			if ("M".equals(modeTextValue)) {
				modeTextValue = "A";

			} else if ("T".equals(modeTextValue)) {
				modeTextValue = "A";

			} else if ("A".equals(modeTextValue)) {
				// setBoostMode(boostDurationInSeconds);
				modeTextValue = "B";
				currentTemp = maxTemp;
				boostMode = true;

			} else if ("B".equals(modeTextValue)) {
				setBoostMode(0);
				modeTextValue = "M";
			}

			if (modeChangeListener != null) {
				modeChangeListener.onModeChanged(getMode());
			}
			invalidate();
		}

	}




	class BoostUpdateTimerListener implements ValueAnimator.AnimatorUpdateListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.animation.ValueAnimator.AnimatorUpdateListener#onAnimationUpdate(android.animation.ValueAnimator)
		 */
		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			int boostSecondsToRunNew = (int) ((arg0.getDuration() - arg0.getCurrentPlayTime()) / 1000);

			if (boostSecondsToRunNew != boostSecondsToRun) {
				invalidate();
			}

			boostSecondsToRun = boostSecondsToRunNew;
		}
	}

}
