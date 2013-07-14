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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ambient.control.R;
import org.ambientlight.process.NodeConfiguration;
import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.process.handler.expression.DecisionHandlerConfiguration;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Scroller;


public class ProcessCardDrawer extends View implements OnTouchListener {

	private ProcessConfiguration process;


	public ProcessConfiguration getProcess() {
		return process;
	}


	public void setProcess(ProcessConfiguration process) {
		this.process = process;
		this.contentBitmap = null;

		createNodeSnippetsRecursively(0, 0);
		calculateContentDimensions();
		createProcessCardBitmap();

		this.invalidate();
	}

	private Bitmap contentBitmap = null;
	private final GestureDetector mDetector;
	private Scroller mScroller;
	private ValueAnimator mScrollAnimator;

	private final int nodeHeight = 100;
	private final int nodeWidth = 200;
	private final int spaceHorizontal = 100;
	private final int spaceVertical = 100;

	private int contentHeight = 0;
	private int contentWidth = 0;

	private int shiftX = 0;
	private int shiftY = 0;

	private int lastX = 0;
	private int lastY = 0;

	HashMap<Integer, ArrayList<Bitmap>> nodeSnippets = new HashMap<Integer, ArrayList<Bitmap>>();

	List<Point> points = new ArrayList<Point>();
	Paint paint = new Paint();


	public ProcessCardDrawer(Context context, AttributeSet attrs) {
		super(context, attrs);

		setFocusable(true);
		setFocusableInTouchMode(true);
		this.setOnTouchListener(this);

		// Create a gesture detector to handle onTouch messages
		mDetector = new GestureDetector(this.getContext(), new GestureListener());

		// Turn off long press--this control doesn't use it, and if long press
		// is enabled,
		// you can't scroll for a bit, pause, then scroll some more (the pause
		// is interpreted
		// as a long press, apparently)
		mDetector.setIsLongpressEnabled(false);

		// Create a Scroller to handle the fling gesture.
		if (Build.VERSION.SDK_INT < 11) {
			mScroller = new Scroller(getContext());
		} else {
			mScroller = new Scroller(getContext(), null, true);
		}
		// The scroller doesn't have any built-in animation functions--it just
		// supplies
		// values when we ask it to. So we have to have a way to call it every
		// frame
		// until the fling ends. This code (ab)uses a ValueAnimator object to
		// generate
		// a callback on every animation frame. We don't use the animated value
		// at all.
		if (Build.VERSION.SDK_INT >= 11) {
			mScrollAnimator = ValueAnimator.ofFloat(0, 1);
			mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator valueAnimator) {
					tickScrollAnimation();
				}
			});
		}
		// this.accelerate();
	}


	/**
	 * @return
	 */
	public void createProcessCardBitmap() {
		contentBitmap = Bitmap.createBitmap(this.contentWidth, this.contentHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(contentBitmap);

		Rect nodeRect = new Rect(0, 0, this.nodeWidth, this.nodeHeight);
		for (int i = 0; i < this.nodeSnippets.size(); i++) {
			for (int y = 0; y < this.nodeSnippets.get(i).size(); y++) {
				Bitmap snippet = this.nodeSnippets.get(i).get(y);

				int xPos = y * (this.nodeWidth + this.spaceHorizontal);
				int yPos = i * (this.nodeHeight + this.spaceVertical);
				int xOff = xPos + this.nodeWidth;
				int yOff = yPos + this.nodeHeight;

				Rect pasteRect = new Rect(xPos, yPos, xOff, yOff);
				canvas.drawBitmap(snippet, nodeRect, pasteRect, null);
			}
		}
	}


	/**
	 * 
	 */
	public void calculateContentDimensions() {
		int rows = this.nodeSnippets.keySet().size();
		int columns = 0;
		for (List<Bitmap> current : this.nodeSnippets.values()) {
			if (current.size() > columns) {
				columns = current.size();
			}
		}
		this.contentHeight = rows * (this.nodeHeight + this.spaceVertical);
		this.contentWidth = columns * (this.nodeWidth + this.spaceHorizontal);
	}


	/**
	 * 
	 */
	private void createNodeSnippetsRecursively(int id, int rowNumber) {
		NodeConfiguration current = process.nodes.get(id);

		Bitmap result = this.createNode(current.actionHandler.getClass().getSimpleName(), 0, 0);
		ArrayList<Bitmap> row = this.nodeSnippets.get(rowNumber);
		if (row == null) {
			row = new ArrayList<Bitmap>();
			this.nodeSnippets.put(rowNumber, row);
		}
		row.add(result);

		if (current.actionHandler.nextNodeId == null)
			return;

		createNodeSnippetsRecursively(current.actionHandler.nextNodeId, rowNumber + 1);

		if (current.actionHandler instanceof DecisionHandlerConfiguration) {
			DecisionHandlerConfiguration currentHandler = (DecisionHandlerConfiguration) current.actionHandler;
			createNodeSnippetsRecursively(currentHandler.nextAlternativeNodeId, rowNumber + 1);
		}
	}


	private void tickScrollAnimation() {
		if (!mScroller.isFinished()) {
			mScroller.computeScrollOffset();
			this.shiftX = mScroller.getCurrX();
			this.shiftY = mScroller.getCurrY();
			invalidate();
		} else {
			mScrollAnimator.cancel();
		}
	}


	@Override
	public void onDraw(Canvas canvas) {
		if (this.isInEditMode()) {
			Bitmap bitmap = this.createNode("ProcessCard", 0, 0);
			Rect src = new Rect(0, 0, this.nodeWidth, this.nodeHeight);
			Rect dest = new Rect(20, 20, this.nodeWidth * 2, this.nodeHeight * 2);
			canvas.drawBitmap(bitmap, src, dest, null);
			return;
		}
		lastX = shiftX + lastX;
		lastY = shiftY + lastY;
		Log.i("pos", String.valueOf(lastX) + "," + String.valueOf(lastY));
		Rect src = new Rect(lastX, lastY, this.contentWidth, this.contentHeight);
		Rect dest = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());

		canvas.drawBitmap(contentBitmap, src, dest, null);
	}


	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// Let the GestureDetector interpret this event
		boolean result = mDetector.onTouchEvent(event);

		// If the GestureDetector doesn't want this event, do some custom
		// processing.
		// This code just tries to detect when the user is done scrolling by
		// looking
		// for ACTION_UP events.
		if (!result) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				// User is done scrolling, it's now safe to do things like
				// autocenter
				// stopScrolling();
				result = true;
			}
		}
		return result;
	}

	/**
	 * Extends {@link GestureDetector.SimpleOnGestureListener} to provide custom
	 * gesture processing.
	 */
	private class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			// Set the pie rotation directly.

			shiftX = (int) distanceX;
			shiftY = (int) distanceY;
			invalidate();
			return true;
		}


		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			// Set up the Scroller for a fling
			mScroller.fling(lastX, lastY, (int) velocityX / 4, (int) velocityY / 4, Integer.MIN_VALUE, Integer.MAX_VALUE,
					Integer.MIN_VALUE, Integer.MAX_VALUE);

			// Start the animator and tell it to animate for the expected
			// duration of the fling.
			if (Build.VERSION.SDK_INT >= 11) {
				mScrollAnimator.setDuration(mScroller.getDuration());
				mScrollAnimator.start();
			}
			return true;
		}


		@Override
		public boolean onDown(MotionEvent e) {
			// // The user is interacting with the pie, so we want to turn on
			// // acceleration
			// // so that the interaction is smooth.
			// mPieView.accelerate();
			// if (isAnimationRunning()) {
			// stopScrolling();
			// }
			return true;
		}
	}


	/**
	 * Enable hardware acceleration (consumes memory)
	 */
	public void accelerate() {
		setLayerToHW(this);
	}


	/**
	 * Disable hardware acceleration (releases memory)
	 */
	public void decelerate() {
		setLayerToSW(this);
	}


	private void setLayerToSW(View v) {
		if (!v.isInEditMode() && Build.VERSION.SDK_INT >= 11) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}


	private void setLayerToHW(View v) {
		if (!v.isInEditMode() && Build.VERSION.SDK_INT >= 11) {
			setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
	}


	private Bitmap createNode(String label, int x, int y) {
		Bitmap bitmap = Bitmap.createBitmap(nodeWidth, nodeHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Resources res = this.getContext().getResources();
		Drawable myImage = res.getDrawable(R.drawable.process_node_background);
		myImage.setBounds(x, y, x + nodeWidth, y + nodeHeight);
		myImage.draw(canvas);
		Paint textPaint = new Paint();
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(20);
		textPaint.setAntiAlias(true);
		textPaint.setShadowLayer(2.0f, 1.0f, 1.0f, Color.BLACK);
		canvas.drawText(label, x + nodeWidth / 2, y + nodeHeight / 2, textPaint);
		return bitmap;
	}

}
