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

import org.ambientlight.annotations.TypeDef;

import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


/**
 * @author Florian Bornkessel
 * 
 */
public class NumericField {

	/**
	 * @param config
	 * @param container
	 * @param field
	 * @param typedef
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public static void createView(final Object config, LinearLayout container, final Field field, TypeDef typedef,
			LinearLayout contentArea) throws IllegalAccessException {
		final double min = Double.parseDouble(typedef.min());
		final double difference = Double.parseDouble(typedef.max()) - min;

		SeekBar seekBar = new SeekBar(container.getContext());
		seekBar.setMax(256);
		double doubleValue = ((((Number) field.get(config)).doubleValue()) - min) / difference;
		seekBar.setProgress((int) (doubleValue * 256.0));
		contentArea.addView(seekBar);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}


			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}


			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				double result = (progress / 256.0) * difference + min;

				if (field.getType().equals(Double.TYPE)) {
					try {
						field.setDouble(config, result);
					} catch (Exception e) {
						// this should not happen
					}
				}

				if (field.getType().equals(Integer.TYPE)) {
					try {
						field.setInt(config, (int) result);
					} catch (Exception e) {
						// this should not happen
					}
				}
			}
		});
	}
}
