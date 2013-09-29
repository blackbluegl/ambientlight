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

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;


/**
 * @author Florian Bornkessel
 *
 */
public class BooleanField {

	/**
	 * @param config
	 * @param container
	 * @param field
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public static void createView(final Object config, LinearLayout container, final Field field, LinearLayout contentArea)
			throws IllegalAccessException {

		final CheckBox checkbox = new CheckBox(container.getContext());
		contentArea.addView(checkbox);

		checkbox.setChecked(field.getBoolean(config));
		if (checkbox.isChecked()) {
			checkbox.setText("aktiviert");
		} else {
			checkbox.setText("deaktiviert");
		}

		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton paramCompoundButton, boolean paramBoolean) {
				try {
					field.setBoolean(config, paramBoolean);
					if (paramBoolean) {
						checkbox.setText("aktiviert");
					} else {
						checkbox.setText("deaktiviert");
					}

				} catch (Exception e) {
					// this should not happen
				}
			}
		});
	}
}
