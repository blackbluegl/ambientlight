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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;


/**
 * @author Florian Bornkessel
 * 
 */
public class ColorChooserView extends View {

	private int hueHeight = 30;
	private int verticalSpacer = 10;

	private OnColorChangedListener listener;

	private Paint hPaint = new Paint();
	private Paint sPaint = new Paint();
	private Paint vPaint = new Paint();
	private Paint strokePaint = new Paint();

	private float relativeHueCoordinateX = 0;
	private float relativeSVCoordinateX = 0;
	private float relativeSVCoordinateY = 1;

	Bitmap hueMap = null;
	Bitmap svMap = null;

	public interface OnColorChangedListener {

		void colorChanged(int color);
	}


	public ColorChooserView(Context context) {
		super(context);
	}


	public ColorChooserView(Context context, OnColorChangedListener listener, int color) {
		super(context);
		this.listener = listener;
		this.setChoosenColor(color);
	}


	public ColorChooserView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		init();
	}


	private void init() {

		// have shaddows
		setLayerType(LAYER_TYPE_SOFTWARE, null);

		// init Stroke Paint
		strokePaint = new Paint();
		strokePaint.setStrokeWidth(2f);
		strokePaint.setColor(Color.WHITE);
		strokePaint.setStyle(Style.STROKE);
		strokePaint.setShadowLayer(2, 1, 1, Color.BLACK);
		strokePaint.setAntiAlias(true);

		// hue colors
		int[] colors = new int[7];
		colors[0] = Color.RED;
		colors[1] = Color.MAGENTA;
		colors[2] = Color.BLUE;
		colors[3] = Color.CYAN;
		colors[4] = Color.GREEN;
		colors[5] = Color.YELLOW;
		colors[6] = Color.RED;

		// positions in gradient for hue chooser
		float[] colorPositions = new float[] { 0 / 6f, 1 / 6f, 2 / 6f, 3 / 6f, 4 / 6f, 5 / 6f, 6 / 6f };
		hPaint = new Paint();
		hPaint.setShader(new LinearGradient(0, 0, getWidth(), 0, colors, colorPositions, Shader.TileMode.MIRROR));
		hueMap = Bitmap.createBitmap(getWidth(), hueHeight, Bitmap.Config.ARGB_8888);
		Canvas hueCanvas = new Canvas(hueMap);
		hueCanvas.drawRect(0, 0, getWidth(), hueHeight, hPaint);

		// init position for SV-Chooser
		int svHeight = getHeight() - hueHeight - verticalSpacer;

		// layer 1 satuarionGradient
		int hueChoosen = Color.HSVToColor(new float[] { (1 - relativeHueCoordinateX) * 360f, 1, 1 });
		sPaint = new Paint();
		sPaint.setShader(new LinearGradient(0, 0, getWidth(), 0, Color.WHITE, hueChoosen, TileMode.MIRROR));
		// layer 2 valueGradient
		vPaint = new Paint();
		vPaint.setShader(new LinearGradient(0, 0, 0, svHeight, Color.argb(0, 0, 0, 0), Color.BLACK, TileMode.MIRROR));
		svMap = Bitmap.createBitmap(getWidth(), svHeight, Bitmap.Config.ARGB_8888);
		Canvas svCanvas = new Canvas(svMap);
		svCanvas.drawRect(0, 0, getWidth(), svHeight, sPaint);
		svCanvas.drawRect(0, 0, getWidth(), svHeight, vPaint);
	}


	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(hueMap, 0, 0, null);
		canvas.drawBitmap(svMap, 0, verticalSpacer + hueHeight, null);

		float absoluteHueCoordinateX = relativeHueCoordinateX * getWidth();
		canvas.drawLine(absoluteHueCoordinateX, 0, absoluteHueCoordinateX, hueHeight, strokePaint);

		float absoluteSVCoordinateX = relativeSVCoordinateX * getWidth();
		float absoluteSVCoordinateY = (verticalSpacer + hueHeight) + relativeSVCoordinateY
				* (getHeight() - verticalSpacer - hueHeight);
		canvas.drawCircle(absoluteSVCoordinateX, absoluteSVCoordinateY, 5, strokePaint);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_UP)
			return true;
		float x = event.getX();
		float y = event.getY();

		// if hue changes the corresponding sv map needs to be updated
		boolean updateMaps = false;

		// update coordinate for hue area
		if (y < hueHeight + verticalSpacer / 2) {
			this.relativeHueCoordinateX = x / getWidth();
			updateMaps = true;
		}// update coordinate for sv area
		else {
			// correct value if out of border
			if (y < hueHeight + verticalSpacer) {
				y = hueHeight + verticalSpacer;
			}

			this.relativeSVCoordinateX = x / getWidth();
			// normalize y pos
			y = y - hueHeight - verticalSpacer;
			// invert because bottom is black
			this.relativeSVCoordinateY = y / svMap.getHeight();
		}

		if (updateMaps) {
			init();
		}
		invalidate();

		if (listener != null) {
			listener.colorChanged(getChoosenColor());
		}

		return true;
	}


	public int getChoosenColor() {
		float[] hsv = new float[3];
		hsv[0] = 360 * (1 - this.relativeHueCoordinateX);
		hsv[1] = this.relativeSVCoordinateX;
		hsv[2] = 1 - this.relativeSVCoordinateY;
		return Color.HSVToColor(hsv);
	}


	public void setChoosenColor(int colorChoosen) {
		float[] hsvValues = new float[3];
		android.graphics.Color.RGBToHSV(Color.red(colorChoosen), Color.green(colorChoosen), Color.blue(colorChoosen), hsvValues);
		this.relativeHueCoordinateX = 1 - hsvValues[0] / 360f;
		this.relativeSVCoordinateX = hsvValues[1];
		this.relativeSVCoordinateY = 1 - hsvValues[2];
	}


	@SuppressLint("DrawAllocation")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// calculate a height (and width) that is always visible if no layout restrictions are made. it is useful for scrollable
		// views. In portrait mode it does not affect the desired size because the display width will be used. In landscape it is
		// enough to get sure that the widget can be fully shown. remember this value will only be used if the container does not
		// make any restriction to the height.
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int dispWidth = size.x;
		int dispHeight = (int) (size.y * 0.66f); // a little less for menues

		// int desiredSize = dispHeight < dispWidth ? dispHeight : dispWidth;

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		int height;

		// Measure Width
		if (widthMode == MeasureSpec.EXACTLY) {
			// Must be this size
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			// Can't be bigger than...
			width = widthSize < dispWidth ? widthSize : dispWidth;
		} else {
			// Be whatever you want
			width = dispWidth;
		}

		// Measure Height
		if (heightMode == MeasureSpec.EXACTLY) {
			// Must be this size
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			// Can't be bigger than...
			height = heightSize < dispHeight ? heightSize : dispHeight;
		} else {
			// Be whatever you want
			height = dispHeight;

		}

		// this widget does not work if it is too small
		if (width < 20) {
			width = 20;
		}
		if (height < hueHeight + verticalSpacer + 20) {
			height = hueHeight + verticalSpacer + 20;
		}

		// MUST CALL THIS
		setMeasuredDimension(width, height);
	}

}
