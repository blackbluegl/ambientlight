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

import org.ambient.control.R;
import org.ambient.control.config.ConfigBindingHelper;
import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambient.control.config.classhandlers.WhereToPutConfigurationData.WhereToPutType;
import org.ambient.util.GuiUtils;
import org.ambientlight.room.RoomConfiguration;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;


/**
 * @author Florian Bornkessel
 *
 */
public class SimpleListField {

	/**
	 * @param config
	 * @param field
	 * @param altValues
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public static void createView(final EditConfigHandlerFragment context, final Object config, final Field field,
			List<String> altValues, LinearLayout contentArea, final String selectedServer, final RoomConfiguration roomConfig)
			throws IllegalAccessException {
		final ListView listView = new ListView(contentArea.getContext());
		contentArea.addView(listView);
		@SuppressWarnings({ "unchecked" })
		// set by FieldType.SIMPLE_LIST
		final List<Object> listContent = (List<Object>) field.get(config);
		final List<String> altValuesFinal = altValues;

		final ImageView createNew = new ImageView(context.getActivity());
		contentArea.addView(createNew);
		createNew.setImageResource(R.drawable.content_new);
		createNew.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WhereToPutConfigurationData whereToStore = new WhereToPutConfigurationData();
				whereToStore.fieldName = field.getName();
				whereToStore.type = WhereToPutType.LIST;
				whereToStore.positionInList = 0;
				context.whereToPutDataFromChild = whereToStore;

				EditConfigHandlerFragment.createNewConfigBean(altValuesFinal,
						ConfigBindingHelper.toCharSequenceArray(altValuesFinal), context, selectedServer, roomConfig);
			}
		});
		if (listContent.size() > 0) {
			createNew.setVisibility(View.GONE);
		}
		final ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(context.getActivity(), android.R.layout.simple_list_item_1,
				listContent);

		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		GuiUtils.setListViewHeightBasedOnChildren(listView);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {

				Object valueAtPosition = adapter.getItem(paramInt);

				EditConfigHandlerFragment.editConfigBean(context, valueAtPosition, selectedServer, roomConfig);
			}
		});

		listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			List<Integer> checkedItems = new ArrayList<Integer>();


			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

				switch (item.getItemId()) {

				case R.id.menuEntryRemoveConfigurationClass:

					List<Object> list = new ArrayList<Object>();
					for (Integer position : checkedItems) {
						list.add(adapter.getItem(position));
					}

					for (Object current : list) {
						listContent.remove(current);
					}
					adapter.notifyDataSetChanged();
					if (adapter.isEmpty()) {
						createNew.setVisibility(View.VISIBLE);
					}
					mode.finish();
					GuiUtils.setListViewHeightBasedOnChildren(listView);
					break;
				case R.id.menuEntryAddConfigurationClass:
					WhereToPutConfigurationData whereToStore = new WhereToPutConfigurationData();
					whereToStore.fieldName = field.getName();
					whereToStore.type = WhereToPutType.LIST;
					whereToStore.positionInList = 0;
					context.whereToPutDataFromChild = whereToStore;

					EditConfigHandlerFragment.createNewConfigBean(altValuesFinal,
							ConfigBindingHelper.toCharSequenceArray(altValuesFinal), context, selectedServer, roomConfig);
					mode.finish();
					break;
				}
				return false;
			}


			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.fragment_edit_configuration_simple_list_cab, menu);
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

			}
		});
	}

}
