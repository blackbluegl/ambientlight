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

package org.ambient.control.config;

import java.lang.reflect.Field;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.ws.Room;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;


/**
 * creates an gui element with a spinner where a user can choose a bean from the altValues list. the chosen bean will be set to
 * the given field of the given bean. you cannot create new beans with this gui element and you have to annotate alternative
 * values to have a useful setting.
 * 
 * @author Florian Bornkessel
 * 
 */
public class BeanSelectionField extends FieldGenerator {

	private static final String LOG = "BeanSelectionField";


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
	public BeanSelectionField(Room roomConfig, Object config, Field field, EditConfigFragment context,
			LinearLayout contentArea) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		super(roomConfig, config, field, context, contentArea);
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
			// create spinner
			Spinner spinner = new Spinner(contentArea.getContext());
			contentArea.addView(spinner);

			final ArrayAdapter<String> adapter = new ArrayAdapter<String>(contextFragment.getActivity(),
					android.R.layout.simple_spinner_item, altValuesToDisplay);
			adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
			spinner.setAdapter(adapter);

			int positionOfSelection = 0;
			for (Object current : altValues) {
				if (current.equals(field.get(bean))) {
					positionOfSelection = altValues.indexOf(current);
				}
			}

			spinner.setSelection(positionOfSelection);

			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> paramAdapterView, View paramView, int position, long paramLong) {
					try {
						field.set(bean, altValues.get(position));
					} catch (Exception e) {
						Log.e(LOG, "Could not set bean to field!", e);
						Toast.makeText(contentArea.getContext(), "Could not set bean to field!", Toast.LENGTH_SHORT).show();
					}
				}


				@Override
				public void onNothingSelected(AdapterView<?> arg0) {

				}
			});
		}
	}

}
