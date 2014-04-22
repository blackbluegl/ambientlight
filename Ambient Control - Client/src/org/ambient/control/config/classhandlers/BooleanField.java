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

import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambientlight.ws.Room;

import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Toast;


/**
 * creates an gui element with a checkbox where a user can switch a boolean field. You cannot annotate alternative values. This
 * does not make sense here.
 * 
 * @author Florian Bornkessel
 * 
 */
public class BooleanField extends FieldGenerator {

	public static final String LOG = "BooleanField";


	/**
	 * @param roomConfig
	 * @param bean
	 * @param field
	 * @param contextFragment
	 * @param contentArea
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 */
	public BooleanField(Room roomConfig, Object bean, Field field, EditConfigHandlerFragment contextFragment,
			LinearLayout contentArea) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		super(roomConfig, bean, field, contextFragment, contentArea);
		// TODO Auto-generated constructor stub
	}


	/**
	 * @param config
	 * @param container
	 * @param field
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public void createView() throws IllegalAccessException {

		final CheckBox checkbox = new CheckBox(contentArea.getContext());
		contentArea.addView(checkbox);

		boolean isChecked = checkbox.isChecked();
		checkbox.setText(isChecked ? "aktiviert" : "deaktiviert");

		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton paramCompoundButton, boolean isChecked) {
				try {
					field.setBoolean(bean, isChecked);

					checkbox.setText(isChecked ? "aktiviert" : "deaktiviert");

				} catch (Exception e) {
					Log.e(LOG, "Could not set value to field!", e);
					Toast.makeText(contentArea.getContext(), "Could not set value to field!", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}
