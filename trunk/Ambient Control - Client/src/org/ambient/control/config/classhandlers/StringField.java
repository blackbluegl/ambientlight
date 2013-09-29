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
import java.util.List;

import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambientlight.annotations.AlternativeValues;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;


/**
 * @author Florian Bornkessel
 * 
 */
public class StringField {

	/**
	 * @param config
	 * @param container
	 * @param field
	 * @param altValues
	 * @param altValuesToDisplay
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public static void createView(EditConfigHandlerFragment context, final Object config, LinearLayout container,
			final Field field, final List<String> altValues, List<String> altValuesToDisplay, LinearLayout contentArea)
					throws IllegalAccessException {

		if (field.getAnnotation(AlternativeValues.class) != null) {
			// create spinner

			Spinner spinner = new Spinner(container.getContext());
			contentArea.addView(spinner);

			final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context.getActivity(),
					android.R.layout.simple_spinner_item, altValuesToDisplay);
			adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
			spinner.setAdapter(adapter);

			String selection = (String) field.get(config);
			int positionOfSelection = altValuesToDisplay.indexOf(selection);
			if (positionOfSelection < 0) {
				positionOfSelection = 0;
			}
			spinner.setSelection(positionOfSelection);

			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
					String valueToPaste = altValues.get(paramInt);
					try {
						field.set(config, valueToPaste);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}


				@Override
				public void onNothingSelected(AdapterView<?> paramAdapterView) {
				}
			});

		} else {

			// create textfield
			final EditText input = new EditText(container.getContext());
			contentArea.addView(input);
			input.setText((String) field.get(config));

			input.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
					try {
						field.set(config, input.getText().toString());
					} catch (Exception e) {
						// should not happen
					}
				}


				@Override
				public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
				}


				@Override
				public void afterTextChanged(Editable paramEditable) {
				}
			});
		}
	}

}
