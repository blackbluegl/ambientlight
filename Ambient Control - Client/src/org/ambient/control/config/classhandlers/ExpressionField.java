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
import java.util.ArrayList;
import java.util.List;

import org.ambient.control.config.EditConfigHandlerFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;


/**
 * @author Florian Bornkessel
 * 
 */
public class ExpressionField {

	/**
	 * @param config
	 * @param container
	 * @param field
	 * @param altValues
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public static void createView(final EditConfigHandlerFragment context, final Object config, LinearLayout container,
			final Field field, List<String> altValues, LinearLayout contentArea) throws IllegalAccessException {
		// create textfield
		final MultiAutoCompleteTextView input = new MultiAutoCompleteTextView(container.getContext());
		contentArea.addView(input);

		input.setText((String) field.get(config));

		List<String> variablesEnrichedValues = new ArrayList<String>();
		variablesEnrichedValues.add("#{tokenValue}");
		for (String current : altValues) {
			variablesEnrichedValues.add("#{" + current + "}");
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context.getActivity(),
				android.R.layout.simple_dropdown_item_1line, variablesEnrichedValues);
		input.setAdapter(adapter);
		input.showDropDown();
		input.setThreshold(1);
		input.setTokenizer(new SpaceTokenizer());

		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
				try {
					field.set(config, input.getText().toString());
					input.showDropDown();
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
