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
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

		contentArea.addView(list);

		// ParameterizedType pt = (ParameterizedType) field.getGenericType();
		// final String containingClass =
		// pt.getActualTypeArguments()[0].toString().substring(6);
		// list.setTag(containingClass);

		@SuppressWarnings("unchecked")
		// set by FieldType.SELECTION_LIST
		final List<Object> listContent = (List<Object>) field.get(config);

		// show all values incl. potential values that the user may select or
		// unselect
		HashMap<Object, Boolean> displayContentMap = new HashMap<Object, Boolean>();
		for (Object currentObject : listContent) {
			displayContentMap.put(currentObject, true);
		}
		for (Object key : altValues) {
			if (displayContentMap.containsKey(key) == false) {
				displayContentMap.put(key, false);
			}
		}

		List<Object> displayContent = new ArrayList<Object>(displayContentMap.keySet());
		final ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(context.getActivity(), R.layout.layout_checkable_list_item,
				displayContent);
		list.setAdapter(adapter);

		GuiUtils.setListViewHeightBasedOnChildren(list);

		// set checked the values that where stored in listcontent
		for (Object currentKey : displayContent) {
			if (displayContentMap.get(currentKey) == true) {
				LinearLayout rowView = (LinearLayout) adapter.getView(adapter.getPosition(currentKey), null, list);
				CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkBox1);
				checkBox.setChecked(true);
			}
		}

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
				CheckBox checkbox = (CheckBox) paramView.findViewById(R.id.checkBox1);
				checkbox.setChecked(!checkbox.isChecked());
				Object valueAtPosition = adapter.getItem(paramInt);
				// sync view with field content
				if (checkbox.isChecked()) {
					listContent.add(valueAtPosition);
				} else {
					listContent.remove(valueAtPosition);
				}
			}
		});
	}

}
