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
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.ws.Room;

import android.util.Log;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;


/**
 * creates a simple slider that binds to a double value. The borders for the values are given by a typedef object in the
 * createView method.
 * 
 * @author Florian Bornkessel
 * 
 */
public class NumericField extends FieldGenerator {

	public static final String LOG = "NumericField";


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
	public NumericField(Room roomConfig, Object bean, Field field, EditConfigHandlerFragment contextFragment,
			LinearLayout contentArea) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		super(roomConfig, bean, field, contextFragment, contentArea);
	}


	/**
	 * @param config
	 * @param container
	 * @param field
	 * @param typedef
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public void createView(TypeDef typedef) throws IllegalAccessException {

		// borders
		final double min = Double.parseDouble(typedef.min());
		final double difference = Double.parseDouble(typedef.max()) - min;

		// view with max 256 steps
		SeekBar seekBar = new SeekBar(contentArea.getContext());
		contentArea.addView(seekBar);
		seekBar.setMax(256);

		// normalize to minimal value and set into relation of the possible difference
		double doubleValue = ((((Number) field.get(bean)).doubleValue()) - min) / difference;
		// bind to seekbar
		seekBar.setProgress((int) (doubleValue * 256.0));

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}


			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}


			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				// extract field value from slider
				double result = (progress / 256.0) * difference + min;

				try {
					// copy result to fieldvalue
					if (field.getType().equals(Double.TYPE)) {
						field.setDouble(bean, result);
					}
					if (field.getType().equals(Integer.TYPE)) {
						field.setInt(bean, (int) result);
					}
					if (field.getType().equals(Float.TYPE)) {
						field.setFloat(bean, (float) result);
					}
				} catch (Exception e) {
					Log.e(LOG, "Could not set value to field!", e);
					Toast.makeText(contentArea.getContext(), "Could not set value to field!", Toast.LENGTH_SHORT).show();
				}

			}
		});
	}
}