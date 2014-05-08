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
import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.ws.Room;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;


/**
 * creates an gui element to handle string values of beans. AltValues Annotation with AltDisplayValues may be used to create a
 * spinner. The user can choose a display value and the corresponding altValue will be set to the field. If the bound value is not
 * in the altValues list, the first value of the list will be used and the field value will be overwritten. If no annotation is
 * present a simple textbox will be displayed where the user can type in a value by hand.
 * 
 * @author Florian Bornkessel
 * 
 */
public class StringField extends FieldGenerator {

	public static final String LOG = "StringField";


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
	public StringField(Room roomConfig, Object bean, Field field, EditConfigHandlerFragment contextFragment,
			LinearLayout contentArea) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		super(roomConfig, bean, field, contextFragment, contentArea);
	}


	/**
	 * @param bean
	 * @param container
	 * @param field
	 * @param altValues
	 * @param altValuesToDisplay
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public void createView() throws IllegalAccessException {

		if (field.getAnnotation(AlternativeValues.class) != null) {
			// a drop down box with values to choose
			createSpinnerView();

		} else {
			// create textfield for free text input
			createSimpleView();
		}
	}


	/**
	 * @throws IllegalAccessException
	 */
	private void createSimpleView() throws IllegalAccessException {

		final EditText input = new EditText(contextFragment.getActivity());
		contentArea.addView(input);

		// bind value
		input.setText((String) field.get(bean));

		// update field with every entered char
		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
				try {
					field.set(bean, input.getText().toString());
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


	/**
	 * @throws IllegalAccessException
	 */
	private void createSpinnerView() throws IllegalAccessException {

		Spinner spinner = new Spinner(contentArea.getContext());
		contentArea.addView(spinner);

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(contextFragment.getActivity(),
				android.R.layout.simple_spinner_item, altValuesToDisplay);
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinner.setAdapter(adapter);

		// set position of in AltValues List or beginning if not found
		int positionOfSelection = altValues.indexOf(field.get(bean));
		if (positionOfSelection < 0) {
			positionOfSelection = 0;
		}
		spinner.setSelection(positionOfSelection);

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {

				// set selection to bean
				try {
					field.set(bean, altValues.get(paramInt).toString());
				} catch (Exception e) {
					Log.e(LOG, "Could not set value to field!", e);
					Toast.makeText(contentArea.getContext(), "Could not set value to field!", Toast.LENGTH_SHORT).show();
				}
			}


			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

}
