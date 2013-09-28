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
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ambient.control.R;
import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambient.util.GuiUtils;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;


/**
 * @author Florian Bornkessel
 *
 */
public class SelectionListField {

	/**
	 * @param config
	 * @param field
	 * @param altValues
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public static void createView(EditConfigHandlerFragment context, final Object config, final Field field,
			List<String> altValues, LinearLayout contentArea) throws IllegalAccessException {
		final ListView list = new ListView(contentArea.getContext());
		contentArea.addView(list);
		ParameterizedType pt = (ParameterizedType) field.getGenericType();
		final String containingClass = pt.getActualTypeArguments()[0].toString().substring(6);
		list.setTag(containingClass);
		@SuppressWarnings("unchecked")
		// set by FieldType.SELECTION_LIST
		final List<Object> listContent = (List<Object>) field.get(config);
		HashMap<Object, Boolean> tempValues = new HashMap<Object, Boolean>();
		for (Object currentObject : listContent) {
			tempValues.put(currentObject, true);
		}

		for (Object key : altValues) {
			if (tempValues.containsKey(key) == false) {
				tempValues.put(key, false);
			}
		}

		List<Object> contentForArrayAdapter = new ArrayList<Object>(tempValues.keySet());

		final ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(context.getActivity(), R.layout.layout_checkable_list_item,
				contentForArrayAdapter);

		for (Object currentKey : contentForArrayAdapter) {
			if (tempValues.get(currentKey) == true) {
				LinearLayout rowView = (LinearLayout) adapter.getView(adapter.getPosition(currentKey), null, list);
				CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkBox1);
				checkBox.setChecked(true);
			}
		}

		list.setAdapter(adapter);
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		GuiUtils.setListViewHeightBasedOnChildren(list);
		// we skip this if the values are simple and can be handled directly
		// on the view, like booleans

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
				CheckBox checkbox = (CheckBox) paramView.findViewById(R.id.checkBox1);
				checkbox.setChecked(!checkbox.isChecked());
				Object valueAtPosition = adapter.getItem(paramInt);
				if (checkbox.isChecked()) {
					listContent.add(valueAtPosition);
				} else {
					listContent.remove(valueAtPosition);
				}
			}
		});
	}

}
