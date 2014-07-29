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

import org.ambient.control.config.EditConfigFragment;
import org.ambientlight.ws.Room;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;


/**
 * creates an expression gui element with auto spelling feature that displays variable names and the user may add these variables
 * to the expression. The result may be bound to a String field. The alternative values have to be String values. They actually
 * represent sensors that can be read by the process engine.
 * 
 * @author Florian Bornkessel
 * 
 */
public class ExpressionField extends FieldGenerator {

	public static final String LOG = "ExpressionField";


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
	public ExpressionField(Room roomConfig, Object bean, Field field, EditConfigFragment contextFragment,
			LinearLayout contentArea) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		super(roomConfig, bean, field, contextFragment, contentArea);
	}


	public void createView() throws IllegalAccessException {

		// create textfield
		final MultiAutoCompleteTextView input = new MultiAutoCompleteTextView(contentArea.getContext());
		contentArea.addView(input);

		input.setText((String) field.get(bean));

		// display list with sensor variable names
		List<String> variablesEnrichedValues = new ArrayList<String>();
		// hardcode tokenvalue to signal that data from token shall be used in the actionhandler of the process
		variablesEnrichedValues.add("#{tokenValue}");
		// build list by the altValues annotation
		for (Object current : altValues) {
			if (current instanceof String == false) {
				Log.e(LOG, "AltValue is no String and not usefull as sensor variable.");
				Toast.makeText(contentArea.getContext(), "AltValue is no String and not usefull as sensor variable.",
						Toast.LENGTH_SHORT).show();
				continue;
			}
			variablesEnrichedValues.add("#{" + current + "}");
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(contextFragment.getActivity(),
				android.R.layout.simple_dropdown_item_1line, variablesEnrichedValues);
		input.setAdapter(adapter);

		// display alternative sensors to user when the editor comes up
		input.showDropDown();

		// shortest threshold for autocorrecture feature. in this case an '#' shows the sensors to the user
		input.setThreshold(1);

		// multi autocomplete with whitespaces instead of comma
		input.setTokenizer(new SpaceTokenizer());

		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
				try {
					field.set(bean, input.getText().toString());
					input.showDropDown();
				} catch (Exception e) {
					Log.e(LOG, "Could not set value to field!", e);
					Toast.makeText(contentArea.getContext(), "Could not set value to field!", Toast.LENGTH_SHORT).show();
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
