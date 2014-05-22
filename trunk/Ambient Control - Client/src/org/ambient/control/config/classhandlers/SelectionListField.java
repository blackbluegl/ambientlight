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
import org.ambient.util.GuiUtils;
import org.ambientlight.ws.Room;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;


/**
 * creates an gui element to manage ArrayLists with a clickable list. The list is populated by altValues annotation whereby the
 * displayValues will be shown to the user. The user may click on an item to put it into the bound ArrayList. Note: values in the
 * ArrayList that have no representation in the altValues annotation will not be changeable and stay untouched in the list. If the
 * list is null, an empty list will be created. The altValues and the values in the list need to be comparable to each other.
 * 
 * @author Florian Bornkessel
 * 
 */
public class SelectionListField extends FieldGenerator {

	static class ViewModel {

		public String displayName;
		public boolean isChecked;
		public Object altValue;
	}


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
	public SelectionListField(Room roomConfig, Object bean, Field field, EditConfigHandlerFragment contextFragment,
			LinearLayout contentArea) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		super(roomConfig, bean, field, contextFragment, contentArea);
	}


	/**
	 * @param bean
	 * @param field
	 * @param altValues
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("rawtypes")
	public void createView() throws IllegalAccessException {

		// create view
		final ListView list = new ListView(contentArea.getContext());

		contentArea.addView(list);

		// get list or create it
		if (field.get(bean) == null) {
			field.set(bean, new ArrayList());
		}
		final List listContent = (List) field.get(bean);

		// build view model
		final List<ViewModel> displayValues = new ArrayList<ViewModel>();

		for (String currentDisplayValue : altValuesToDisplay) {

			Object currentAltValue = altValues.get(altValuesToDisplay.indexOf(currentDisplayValue));
			Object inDataList = findInDataList(currentAltValue, listContent);

			// prepare view model for each entry in the altValues list
			ViewModel entry = new ViewModel();
			displayValues.add(entry);
			entry.displayName = currentDisplayValue;
			entry.altValue = currentAltValue;
			if (inDataList != null) {
				// use original object reference. this makes the handling easier in the adapter later.
				entry.altValue = inDataList;
				entry.isChecked = true;
			}
		}

		// bind list to view model
		final CheckableListAdapter adapter = new CheckableListAdapter(contextFragment.getActivity(), displayValues, listContent);
		list.setAdapter(adapter);
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

		// adapt height to listsize (we do not want local scrolling in list)
		GuiUtils.setListViewHeightBasedOnChildren(list);

		// add and remove to field value by clicking on an item
		list.setOnItemClickListener(new OnItemClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> paramAdapterView, View view, int position, long paramLong) {

				displayValues.get(position).isChecked = !displayValues.get(position).isChecked;
				Object altValueAtPosition = altValues.get(position);

				// sync view with field content
				if (displayValues.get(position).isChecked) {
					listContent.add(altValueAtPosition);
				} else {
					listContent.remove(altValueAtPosition);
				}
			}
		});
	}


	/**
	 * check if the given alternative value equals an entry in the fields list
	 * 
	 * @param altValue
	 * @param listContent
	 * @return
	 */
	public Object findInDataList(Object altValue, List<Object> listContent) {
		for (Object currentValueInField : listContent) {
			if (altValue.equals(currentValueInField))
				return currentValueInField;
		}
		return null;
	}
}
