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
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;


/**
 * @author Florian Bornkessel
 * 
 */
public class TemperatureModeView extends View {

	int viewWidth;
	int viewHeight;

	Paint bgPaint = new Paint();
	Rect bgRect = new Rect();

	Paint barPaint = new Paint();
	Point[] barPoints;
	Path barPath = new Path();
	int currentBarColor = 0xFFFF1111;

	Paint markerPaint = new Paint();
	RectF markerRect = new RectF();

	TextPaint textPaint = new TextPaint();
	String valueText = "22,5°C";

	int valueChoosen = 50;

	String modeTextValue = "A";
	Paint modeTextPaint = new Paint();

	RectF modeBorderRect = new RectF();
	Paint modeBorderPaint = new Paint();


	/**
	 * @param context
	 * @param attrs
	 */
	public TemperatureModeView(Context context, AttributeSet attrs) {
		super(context, attrs);

		textPaint.setTextAlign(Align.CENTER);
		textPaint.setColor(Color.WHITE);
		textPaint.setAntiAlias(true);
		textPaint.setShadowLayer(2.0f, 1.0f, 1.0f, Color.BLACK);

		Typeface tfText = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
		modeTextPaint.setTypeface(tfText);
		modeTextPaint.setTextAlign(Align.CENTER);
		modeTextPaint.setColor(Color.WHITE);
		modeTextPaint.setAntiAlias(true);
		modeTextPaint.setShadowLayer(2.0f, 1.0f, 1.0f, Color.BLACK);

		bgPaint.setStyle(Paint.Style.FILL);
		bgPaint.setColor(0x22333333);

		barPaint.setStyle(Paint.Style.FILL);
		barPaint.setColor(Color.GREEN);
		barPaint.setAntiAlias(true);

		markerPaint.setStyle(Paint.Style.STROKE);
		markerPaint.setColor(0xBBFFFFFF);
		markerPaint.setAntiAlias(true);

		modeBorderPaint.setStyle(Paint.Style.STROKE);
		modeBorderPaint.setColor(0xFFFFFFFF);
		modeBorderPaint.setAntiAlias(true);
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

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

		// draw marker
		canvas.drawRoundRect(markerRect, 6, 6, markerPaint);

		// draw value text
		canvas.drawText(valueText, viewWidth / 2, viewHeight / 14, textPaint);

		// draw icon
		canvas.drawRoundRect(modeBorderRect, 6, 6, modeBorderPaint);
		canvas.drawText(modeTextValue, modeBorderRect.centerX(), modeBorderRect.centerY()
				- ((modeTextPaint.descent() + modeTextPaint.ascent()) / 2), modeTextPaint);
	}


	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		viewWidth = xNew;
		viewHeight = yNew;

		// calculate points for gauge
		int offSetX = viewWidth / 10;
		int offSetYTop = viewHeight / 10;
		int offsetYBottom = viewHeight / 6;

		float centerY = viewHeight - (offsetYBottom / 2.0f);
		float lengthX = viewWidth - 2 * offSetX;
		float lengthY = offsetYBottom;
		float sideLength = 0;
		if (lengthX > lengthY) {
			sideLength = lengthY * 0.8f;
		} else {
			sideLength = lengthX * 0.8f;

		}
		modeBorderRect.left = offSetX;
		modeBorderRect.bottom = centerY + sideLength / 2.0f;
		modeBorderRect.right = offSetX + sideLength;
		modeBorderRect.top = centerY - sideLength / 2.0f;
		modeBorderPaint.setStrokeWidth((viewHeight + viewWidth) / 128);
		modeTextPaint.setTextSize(sideLength);

		barPoints = new Point[] { new Point(offSetX, offSetYTop), new Point(viewWidth - offSetX, offSetYTop),
				new Point((int) (offSetX + sideLength), viewHeight - offsetYBottom),
				new Point(offSetX, viewHeight - offsetYBottom) };

		int[] colors = new int[5];
		colors[0] = getColor(10f, currentBarColor);
		colors[1] = getColor(2.5f, currentBarColor);
		colors[2] = getColor(1.5f, currentBarColor);
		colors[3] = getColor(0.8f, currentBarColor);
		colors[4] = getColor(0.2f, currentBarColor);

		float[] positions = new float[] { 0f, 0.3f, 0.5f, 0.51f, 1.0f };

		barPaint.setShader(new LinearGradient(0, 0, 0, viewHeight, colors, positions, Shader.TileMode.MIRROR));

		float valueSize = 0;
		if (viewHeight / 6 < viewWidth) {
			valueSize = viewHeight / 16;
		} else {
			valueSize = viewWidth / 3;
		}
		textPaint.setTextSize(valueSize);

		markerRect.left = (int) (offSetX * 0.9);
		markerRect.right = viewWidth - offSetX;
		markerRect.bottom = (offSetYTop) + (valueChoosen / 100.0f) * (viewHeight - offsetYBottom - offSetYTop);
		markerRect.top = markerRect.bottom - viewHeight / 60;
		markerPaint.setStrokeWidth((viewHeight - offsetYBottom - offSetYTop) / 98);
		markerPaint.setShadowLayer(3.0f, 2.0f, 2.0f, Color.GREEN);
	}


	int getColor(float factor, int color) {
		int rNew = (int) (Color.red(color) * factor > 255 ? 255 : Color.red(color) * factor);
		int gNew = (int) (Color.green(color) * factor > 255 ? 255 : Color.green(color) * factor);
		int bNew = (int) (Color.blue(color) * factor > 255 ? 255 : Color.blue(color) * factor);
		return Color.rgb(rNew, gNew, bNew);
	}
}
