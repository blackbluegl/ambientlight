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

package org.ambient.control.config.classhandlers;

import java.lang.reflect.Field;

import org.ambient.views.ColorPickerView;

import android.widget.LinearLayout;


/**
 * @author Florian Bornkessel
 * 
 */
public class ColorField {

	/**
	 * @param config
	 * @param container
	 * @param field
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public static void createView(final Object config, LinearLayout container, final Field field, LinearLayout contentArea)
			throws IllegalAccessException {
		ColorPickerView.OnColorChangedListener listener = new ColorPickerView.OnColorChangedListener() {

			@Override
			public void colorChanged(int color) {
				try {
					field.setInt(config, color);
				} catch (Exception e) {
					// this should not happen
				}
			}
		};

		ColorPickerView colorPickerView = new ColorPickerView(container.getContext(), listener, field.getInt(config));
		contentArea.addView(colorPickerView);
	}

}
