/*
 * Copyright (C) 2011 Devmil (Michael Lamers) 
 * Mail: develmil@googlemail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.devmil.common.ui.color;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost.TabContentFactory;

public class ColorSelectorView extends LinearLayout {
	private static final String HSV_TAG = "HSV";


	private HsvSelectorView hsvSelector;
	
	private int maxHeight = 0;
	private int maxWidth = 0;

	private int color;
	
	private OnColorChangedListener listener;

	public ColorSelectorView(Context context) {
		super(context);
		init();
	}

	public ColorSelectorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public void setColor(int color) {
		setColor(color, null);
	}

	private void setColor(int color, View sender) {
		if (this.color == color)
			return;
		this.color = color;
		if (sender != hsvSelector)
			hsvSelector.setColor(color);
		onColorChanged();
	}

	public int getColor() {
		return color;
	}

	private void init() {
		
		hsvSelector = new HsvSelectorView(getContext());
		hsvSelector.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				400));
		hsvSelector
				.setOnColorChangedListener(new HsvSelectorView.OnColorChangedListener() {
					@Override
					public void colorChanged(int color) {
						setColor(color);
					}
				});
		addView(hsvSelector);
		
	}

	class ColorTabContentFactory implements TabContentFactory {
		@Override
		public View createTabContent(String tag) {
			if (HSV_TAG.equals(tag)) {
				return hsvSelector;
			}
			return null;
		}
	}
	
	private void onColorChanged()
	{
		if(listener != null)
			listener.colorChanged(getColor());
	}
	
	public void setOnColorChangedListener(OnColorChangedListener listener)
	{
		this.listener = listener;
	}
	
	public interface OnColorChangedListener
	{
		public void colorChanged(int color);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			maxHeight = getMeasuredHeight();
			maxWidth = getMeasuredWidth();
		setMeasuredDimension(maxWidth, maxHeight);
	}
}
