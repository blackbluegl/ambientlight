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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ambient.control.R;
import org.ambient.control.config.ConfigBindingHelper;
import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambient.control.config.classhandlers.WhereToPutConfigurationData.WhereToPutType;
import org.ambient.util.GuiUtils;
import org.ambient.views.adapter.EditConfigMapAdapter;
import org.ambientlight.annotations.AlternativeIds;
import org.ambientlight.room.RoomConfiguration;

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
import android.widget.TextView;


/**
 * @author Florian Bornkessel
 * 
 */
public class MapField {

	/**
	 * @param config
	 * @param field
	 * @param altValues
	 * @param altValuesToDisplay
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public static void createView(final EditConfigHandlerFragment context, final Object config, final Field field,
			List<String> altValues, List<String> altValuesToDisplay, LinearLayout contentArea, final String selectedServer,
			final RoomConfiguration roomConfig) throws IllegalAccessException {
		List<String> additionalIds = ConfigBindingHelper.getAlternativeIds(field.getAnnotation(AlternativeIds.class), roomConfig);

		final ListView list = new ListView(contentArea.getContext());
		contentArea.addView(list);
		ParameterizedType pt = (ParameterizedType) field.getGenericType();
		final String containingClass = pt.getActualTypeArguments()[1].toString().substring(6);
		list.setTag(containingClass);

		@SuppressWarnings("unchecked")
		final Map<Object, Object> map = (Map<Object, Object>) field.get(config);
		final Map<String, Object> arrayMap = new LinkedHashMap<String, Object>();

		for (String key : additionalIds) {
			if (map.containsKey(key) == false) {
				arrayMap.put(key, null);
			} else {
				arrayMap.put(key, map.get(key));
			}
		}

		final EditConfigMapAdapter adapter = new EditConfigMapAdapter(context.getFragmentManager(), context.getActivity(),
				arrayMap, map, containingClass);
		list.setAdapter(adapter);
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		GuiUtils.setListViewHeightBasedOnChildren(list);
		// we skip this if the values are simple and can be handled directly
		// on the view, like booleans
		if (containingClass.equals(Boolean.class.getName()) == false) {
			final List<String> altValuesForListener = altValues;
			final CharSequence[] alternativeDisplayValuesForListener = ConfigBindingHelper
					.toCharSequenceArray(altValuesToDisplay);
			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {

					Object valueAtPosition = adapter.getItem(paramInt).getValue();

					WhereToPutConfigurationData whereToStore = new WhereToPutConfigurationData();
					whereToStore.fieldName = field.getName();
					whereToStore.type = WhereToPutType.MAP;
					whereToStore.keyInMap = adapter.getItem(paramInt).getKey();
					context.whereToPutDataFromChild = whereToStore;

					if (valueAtPosition == null && alternativeDisplayValuesForListener.length > 0) {
						EditConfigHandlerFragment.createNewConfigBean(altValuesForListener, alternativeDisplayValuesForListener,
								context, selectedServer, roomConfig);
					} else if (valueAtPosition != null) {

						String currentText = (String) ((TextView) paramView.findViewById(R.id.textViewName)).getText();

						EditConfigHandlerFragment.editConfigBean(context, map.get(currentText), selectedServer, roomConfig);

					}
				}
			});
		}
		list.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			List<Integer> checkedItems = new ArrayList<Integer>();


			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

				switch (item.getItemId()) {

				case R.id.menuEntryRemoveConfigurationClass:

					List<Entry<String, Object>> list = new ArrayList<Entry<String, Object>>();
					for (Integer position : checkedItems) {
						list.add(adapter.getItem(position));
					}

					for (Entry<String, Object> current : list) {
						current.setValue(null);
						map.remove(current.getKey());
					}
					adapter.notifyDataSetChanged();
					break;

				case R.id.menuEntryEditConfigurationClass:

					EditConfigHandlerFragment.editConfigBean(context, adapter.getItem(checkedItems.get(0))
							.getValue(), selectedServer, roomConfig);

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


			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

				if (checked) {
					checkedItems.add(position);
				} else {
					checkedItems.remove(Integer.valueOf(position));
				}
				mode.setTitle(checkedItems.size() + " ausgewählt");

				MenuItem editItem = mode.getMenu().findItem(R.id.menuEntryEditConfigurationClass);
				if (checkedItems.size() == 1 && adapter.getItem(checkedItems.get(0)).getValue() != null) {
					editItem.setVisible(true);
				} else {
					editItem.setVisible(false);
				}
			}
		});
	}
}
