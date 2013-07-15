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
import java.util.Map;

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
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Scroller;


public class ProcessCardDrawer extends View implements OnTouchListener {

	private class NodeSnippet {

		public Bitmap snippet;
		public int rowNumber;
		public int columNumber;
		public List<Integer> drawLineToNodeIds = new ArrayList<Integer>();
	}

	private ProcessConfiguration process;

	private int rows = 0;
	private int columns = 0;

	private final Rect srcRect = new Rect();
	private final Rect destRect = new Rect();

	private Bitmap contentBitmap = null;
	private final GestureDetector mDetector;
	private final Scroller mScroller;
	private final ValueAnimator mScrollAnimator;

	private final int nodeHeight = 100;
	private final int nodeWidth = 200;
	private final int spaceHorizontal = 75;
	private final int spaceVertical = 50;

	private int contentHeight = 0;
	private int contentWidth = 0;

	private int shiftX = 0;
	private int shiftY = 0;

	private int lastX = 0;
	private int lastY = 0;

	Map<Integer, NodeSnippet> nodeSnippets = new HashMap<Integer, NodeSnippet>();

	List<Point> points = new ArrayList<Point>();
	Paint paint = new Paint();


	public ProcessCardDrawer(Context context, AttributeSet attrs) {
		super(context, attrs);

		// setFocusable(true);
		// setFocusableInTouchMode(true);
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
		mScroller = new Scroller(getContext(), null, true);
		// The scroller doesn't have any built-in animation functions--it just
		// supplies
		// values when we ask it to. So we have to have a way to call it every
		// frame
		// until the fling ends. This code (ab)uses a ValueAnimator object to
		// generate
		// a callback on every animation frame. We don't use the animated value
		// at all.
		mScrollAnimator = ValueAnimator.ofFloat(0, 1);
		mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {

				if (!mScroller.isFinished()) {
					mScroller.computeScrollOffset();
					lastX = mScroller.getCurrX();
					lastY = mScroller.getCurrY();
					invalidate();
				} else {
					mScrollAnimator.cancel();
				}
			}
		});
		// this.accelerate();
	}


	/**
	 * @return
	 */
	public void createProcessCardBitmap() {

		int elementWidth = this.nodeWidth + this.spaceHorizontal;
		int elementHeight = this.nodeHeight + this.spaceVertical;

		contentBitmap = Bitmap.createBitmap(this.contentWidth, this.contentHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(contentBitmap);
		// Rect rect = new Rect(0, 0, 300, 300);
		// Paint test = new Paint();
		// test.setColor(Color.WHITE);
		// canvas.drawRect(rect, test);

		Paint linePaint = new Paint();
		linePaint.setColor(Color.GRAY);
		linePaint.setStrokeWidth(1.5f);
		linePaint.setAntiAlias(true);
		linePaint.setShadowLayer(1.5f, 1, 1, Color.BLACK);

		for (NodeSnippet current : this.nodeSnippets.values()) {

			for (Integer nextNodeId : current.drawLineToNodeIds) {
				NodeSnippet next = this.nodeSnippets.get(nextNodeId);
				canvas.drawLine(current.columNumber * elementWidth + this.nodeWidth / 2, current.rowNumber * elementHeight
						+ this.nodeHeight / 2, next.columNumber * elementWidth + this.nodeWidth / 2, next.rowNumber
						* elementHeight + this.nodeHeight / 2, linePaint);
			}
		}

		Rect nodeRect = new Rect(0, 0, this.nodeWidth, this.nodeHeight);
		for (NodeSnippet current : this.nodeSnippets.values()) {

			int xPos = (current.columNumber) * (this.nodeWidth + this.spaceHorizontal);
			int yPos = (current.rowNumber) * (this.nodeHeight + this.spaceVertical);
			int xOff = xPos + this.nodeWidth;
			int yOff = yPos + this.nodeHeight;

			Rect pasteRect = new Rect(xPos, yPos, xOff, yOff);
			canvas.drawBitmap(current.snippet, nodeRect, pasteRect, null);

		}
	}


	/**
	 * 
	 */
	public void calculateContentDimensions() {
		this.contentHeight = this.rows * (this.nodeHeight + this.spaceVertical) - this.spaceVertical;
		this.contentWidth = this.columns * (this.nodeWidth + this.spaceHorizontal) - this.spaceHorizontal;
	}


	/**
	 * 
	 */
	private void createNodeSnippetsRecursively(int id, int rowNumber, int columnNumber) {
		NodeConfiguration current = process.nodes.get(id);

		Bitmap result = this.createNode(current.actionHandler.getClass().getSimpleName(), 0, 0);
		NodeSnippet snippet = new NodeSnippet();
		snippet.columNumber = columnNumber;
		snippet.rowNumber = rowNumber;
		snippet.snippet = result;
		this.nodeSnippets.put(current.id, snippet);

		if (this.columns <= columnNumber) {
			this.columns = columnNumber + 1;
		}
		if (this.rows <= rowNumber) {
			this.rows = rowNumber + 1;
		}

		if (current.actionHandler.nextNodeId == null)
			return;
		snippet.drawLineToNodeIds.add(current.actionHandler.nextNodeId);

		createNodeSnippetsRecursively(current.actionHandler.nextNodeId, rowNumber + 1, columnNumber);

		if (current.actionHandler instanceof DecisionHandlerConfiguration) {
			DecisionHandlerConfiguration currentHandler = (DecisionHandlerConfiguration) current.actionHandler;
			snippet.drawLineToNodeIds.add(currentHandler.nextAlternativeNodeId);
			createNodeSnippetsRecursively(currentHandler.nextAlternativeNodeId, rowNumber + 1, columnNumber + 1);
		}
	}


	@Override
	public void onDraw(Canvas canvas) {
		if (this.isInEditMode()) {
			Bitmap bitmap = this.createNode("ProcessCard", 0, 0);
			srcRect.set(0, 0, this.nodeWidth, this.nodeHeight);
			destRect.set(20, 20, this.nodeWidth * 2, this.nodeHeight * 2);
			canvas.drawBitmap(bitmap, srcRect, destRect, null);
			return;
		}

		lastX = shiftX + lastX;
		if (lastX < 0) {
			lastX = 0;
		} else if (lastX > contentBitmap.getWidth() - canvas.getWidth()) {
			lastX = contentBitmap.getWidth() - canvas.getWidth();
		}
		lastY = shiftY + lastY;
		if (lastY < 0) {
			lastY = 0;
		} else if (lastY > contentBitmap.getHeight() - canvas.getHeight()) {
			lastY = contentBitmap.getHeight() - canvas.getHeight();
		}
		// Log.i("pos", String.valueOf(lastX) + "," + String.valueOf(lastY));

		srcRect.set(lastX, lastY, canvas.getWidth() + lastX, canvas.getHeight() + lastY);
		destRect.set(0, 0, canvas.getWidth(), canvas.getHeight());
		if (srcRect.right - srcRect.left < destRect.right - destRect.left) {
			destRect.right = srcRect.right;
		}
		if (canvas.getHeight() > contentBitmap.getHeight()) {
			destRect.bottom = contentBitmap.getHeight();
			destRect.top = 0;
			srcRect.top = 0;
			srcRect.bottom = contentBitmap.getHeight();
		}
		if (canvas.getWidth() > contentBitmap.getWidth()) {
			destRect.right = contentBitmap.getWidth();
			destRect.left = 0;
			srcRect.left = 0;
			srcRect.right = contentBitmap.getWidth();
		}
		canvas.drawBitmap(contentBitmap, srcRect, destRect, null);
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
			Log.i("GestureListener onScroll: ", distanceX + "," + distanceY);
			shiftX = (int) distanceX;
			shiftY = (int) distanceY;
			invalidate();
			return true;
		}


		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Log.i("GestureListener onFling: ", velocityX + "," + velocityY);

			// Set up the Scroller for a fling
			mScroller.fling(lastX, lastY, -(int) velocityX, -(int) velocityY, 0, contentBitmap.getWidth() - getWidth(), 0,
					contentBitmap.getHeight() - getHeight());

			// Start the animator and tell it to animate for the expected
			// duration of the fling.
			mScrollAnimator.setDuration(mScroller.getDuration());
			mScrollAnimator.start();
			return true;
		}


		@Override
		public boolean onDown(MotionEvent e) {
			Log.i("GestureListener onDown: ", "pressed");
			mScrollAnimator.cancel();
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
		TextPaint textPaint = new TextPaint();
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(15);
		textPaint.setAntiAlias(true);
		textPaint.setShadowLayer(2.0f, 1.0f, 1.0f, Color.BLACK);

		StaticLayout layout = new StaticLayout(label, textPaint, nodeWidth - 10, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
		canvas.translate(nodeWidth / 2, nodeHeight / 2); // position the
		// text
		layout.draw(canvas);
		// canvas.drawText(label, x + nodeWidth / 2, y + nodeHeight / 2,
		// textPaint);
		return bitmap;
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int desiredWidth = 300;
		int desiredHeight = 300;

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
			width = widthSize;
		} else {
			// Be whatever you want
			width = desiredWidth;
		}

		// Measure Height
		if (heightMode == MeasureSpec.EXACTLY) {
			// Must be this size
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			// Can't be bigger than...
			height = heightSize;
		} else {
			// Be whatever you want
			height = desiredHeight;

		}

		// MUST CALL THIS
		setMeasuredDimension(width, height);
	}


	public ProcessConfiguration getProcess() {
		return process;
	}


	public void setProcess(ProcessConfiguration process) {
		this.process = process;
		this.contentBitmap = null;

		createNodeSnippetsRecursively(0, 0, 0);
		calculateContentDimensions();
		createProcessCardBitmap();

		this.invalidate();
	}

}
