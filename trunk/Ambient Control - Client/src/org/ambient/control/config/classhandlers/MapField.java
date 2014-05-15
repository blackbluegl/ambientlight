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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ambient.control.R;
import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambient.control.config.ValueBindingHelper;
import org.ambient.control.config.classhandlers.WhereToMergeBean.WhereToPutType;
import org.ambient.util.GuiUtils;
import org.ambientlight.annotations.AlternativeIds;
import org.ambientlight.ws.Room;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;


/**
 * creates an gui element for hashmap fields. the map is presented as a listView. The alternative ids represent the amount of all
 * entries that will be displayed in the list. The user may create, delete or edit beans that are bound to the key. Note: This
 * element needs the alternative Id annotation. if the field value has got keys which are not in the amount of alternative ids,
 * the key value pair will be ignored and wiped out. Alternative Values are not supported for this element for now. If the hashmap
 * is empty a new one will be created.
 * 
 * @author Florian Bornkessel
 * 
 */
public class MapField extends FieldGenerator {

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
	public MapField(Room roomConfig, Object bean, Field field, EditConfigHandlerFragment contextFragment, LinearLayout contentArea)
			throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		super(roomConfig, bean, field, contextFragment, contentArea);
	}


	/**
	 * @param bean
	 * @param field
	 * @param altValues
	 * @param altValuesToDisplay
	 * @param contentArea
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("rawtypes")
	public void createView(final String selectedRoom) throws IllegalAccessException, ClassNotFoundException,
	InstantiationException {

		final ListView list = new ListView(contentArea.getContext());
		contentArea.addView(list);

		// create an empty hashmap if it is null.
		if (field.get(bean) == null) {
			field.set(bean, new HashMap());
		}

		// have an instance of the field value
		@SuppressWarnings("unchecked")
		final Map<Object, Object> fieldValue = (Map<Object, Object>) field.get(bean);

		// class type of the values elements
		ParameterizedType pt = (ParameterizedType) field.getGenericType();
		final String containingClass = pt.getActualTypeArguments()[1].toString().substring(6);

		// find alternative id's from annotation and prepare a mapping with user friendly keys
		org.ambient.control.config.AlternativeValues alternativeIds = ValueBindingHelper.getValuesForField(
				field.getAnnotation(AlternativeIds.class).values(), bean, roomConfig);

		final Map<Object, String> keyDisplayKeyMapping = new LinkedHashMap<Object, String>();
		for (String currentDisplayKey : alternativeIds.displayValues) {
			keyDisplayKeyMapping.put(alternativeIds.values.get(alternativeIds.displayValues.indexOf(currentDisplayKey)),
					currentDisplayKey);
		}

		// set custom adapter to show user friendly keys in row 1 and a representation of the value in row 2
		final MapAdapter adapter = new MapAdapter(contextFragment.getFragmentManager(), contextFragment.getActivity(),
				keyDisplayKeyMapping, fieldValue, containingClass);
		list.setAdapter(adapter);

		// the listview is embedded in a scrollable view so no scrolling is wanted in this view.
		GuiUtils.setListViewHeightBasedOnChildren(list);

		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

		// we skip this if the values are simple and can be handled directly in the adapter, like booleans
		if (containingClass.equals(Boolean.class.getName()) == false) {

			// a simple click creates a fragment transition to edit the value in a new fragment
			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int position, long paramLong) {

					Object valueAtPosition = adapter.getItem(position).getValue();

					// store position where to merge the edited value
					WhereToMergeBean whereToStore = new WhereToMergeBean();
					whereToStore.fieldName = field.getName();
					whereToStore.type = WhereToPutType.MAP;
					whereToStore.keyInMap = adapter.getItem(position).getKey();
					contextFragment.whereToMergeChildBean = whereToStore;

					// create
					if (valueAtPosition == null) {
						EditConfigHandlerFragment.createNewConfigBean(altClassInstanceValues,
								ValueBindingHelper.toCharSequenceArray(altValuesToDisplay), contextFragment, selectedRoom,
								roomConfig);

					}
					// edit
					else {
						EditConfigHandlerFragment.editConfigBean(contextFragment,
								fieldValue.get(adapter.getItem(position).getKey()), selectedRoom, roomConfig);
					}
				}
			});
		}

		// handle long press events
		list.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			// data model for the context menu
			List<Map.Entry<Object, Object>> checkedItems = new ArrayList<Map.Entry<Object, Object>>();


			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

				// reflect changes to datamodell
				if (checked) {
					checkedItems.add(adapter.getItem(position));
				} else {
					checkedItems.remove(adapter.getItem(position));
				}

				mode.setTitle(checkedItems.size() + " ausgewählt");

				// edit menu button shows up if exactly 1 item is selected in list
				MenuItem editItem = mode.getMenu().findItem(R.id.menuEntryEditConfigurationClass);
				if (checkedItems.size() == 1) {
					// && adapter.getItem(checkedItems.get(0)).getValue() != null
					editItem.setVisible(true);
				} else {
					editItem.setVisible(false);
				}
			}


			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

				switch (item.getItemId()) {

				// user clicked on the delete button
				case R.id.menuEntryRemoveConfigurationClass:

					// remove from reflecting bean and from map adapter
					for (Map.Entry<Object, Object> current : checkedItems) {
						fieldValue.remove(current);
						adapter.remove(current);
					}

					// adapter.notifyDataSetChanged();
					break;

				case R.id.menuEntryEditConfigurationClass:
					// create fragment transition to edit the value of the map entry
					EditConfigHandlerFragment.editConfigBean(contextFragment, checkedItems.get(0).getValue(), selectedRoom,
							roomConfig);
					break;
				}

				return false;
			}


			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.fragment_edit_configuration_cab, menu);
				return true;
			}


			@Override
			public void onDestroyActionMode(ActionMode mode) {
				checkedItems.clear();
			}


			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}
		});
	}
}