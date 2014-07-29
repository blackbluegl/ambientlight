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

import org.ambient.control.config.EditConfigFragment;
import org.ambient.views.ColorPickerView;
import org.ambientlight.ws.Room;

import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;


/**
 * creates an gui element with a color picker where a user can set the color as integer to the bound field. You cannot annotate
 * alternative values. This does not make sense here.
 * 
 * @author Florian Bornkessel
 * 
 */
public class ColorField extends FieldGenerator {

	public static final String LOG = "BooleanField";


	/**
	 * @param roomConfig
	 * @param config
	 * @param field
	 * @param context
	 * @param contentArea
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 */
	public ColorField(Room roomConfig, Object bean, Field field, EditConfigFragment context, LinearLayout contentArea)
			throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		super(roomConfig, bean, field, context, contentArea);
	}


	/**
	 * @param bean
	 * @param container
	 * @param field
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public void createView() throws IllegalAccessException {

		ColorPickerView.OnColorChangedListener listener = new ColorPickerView.OnColorChangedListener() {

			@Override
			public void colorChanged(int color) {
				try {
					field.setInt(bean, color);
				} catch (Exception e) {
					Log.e(LOG, "Could not set value to field!", e);
					Toast.makeText(contentArea.getContext(), "Could not set value to field!", Toast.LENGTH_SHORT).show();
				}
			}
		};

		ColorPickerView colorPickerView = new ColorPickerView(contentArea.getContext(), listener, field.getInt(bean));
		contentArea.addView(colorPickerView);
	}

}
