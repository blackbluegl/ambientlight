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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.ambient.control.R;
import org.ambient.control.config.WhereToMergeBean.WhereToPutType;
import org.ambient.util.GuiUtils;
import org.ambientlight.ws.Room;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
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
public class SimpleListField extends FieldGenerator {

	/**
	 * manages a list of objects and gives the user an ability to instantiate and add new beans from the altClassInstanceValue
	 * annotation. If the ArrayList is null a new instance will be created. If the list is empty an "+" button will be shown. The
	 * user may select elements and
	 * 
	 * @param roomConfig
	 * @param bean
	 * @param field
	 * @param contextFragment
	 * @param contentArea
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 */
	public SimpleListField(Room roomConfig, Object bean, Field field, EditConfigFragment contextFragment, LinearLayout contentArea)
			throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		super(roomConfig, bean, field, contextFragment, contentArea);
	}


	/**
	 * @param bean
	 * @param field
	 * @param altValues
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public void createView(final String selectedRoom) throws IllegalAccessException {

		final ListView listView = new ListView(contentArea.getContext());
		contentArea.addView(listView);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

		// create an empty list if it is null.
		if (field.get(bean) == null) {
			field.set(bean, new ArrayList<Object>());
		}
		@SuppressWarnings({ "unchecked" })
		final List<Serializable> listContent = (List<Serializable>) field.get(bean);

		// bind field value to list model
		final ArrayAdapter<Serializable> adapter = new ArrayAdapter<Serializable>(contextFragment.getActivity(),
				android.R.layout.simple_list_item_1, listContent);
		listView.setAdapter(adapter);

		// create spacer
		View space = new View(contextFragment.getActivity());
		contentArea.addView(space);
		space.setBackgroundResource(R.color.darkGrey);
		space.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1));

		// create a '+' button for an empty list so the user can put in his first object
		final ImageView createNew = new ImageView(contextFragment.getActivity());
		contentArea.addView(createNew);
		createNew.setImageResource(R.drawable.content_new);

		// handle on click and create a new bean in a child fragment
		createNew.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// remember where to store
				WhereToMergeBean whereToStore = new WhereToMergeBean();
				whereToStore.fieldName = field.getName();
				whereToStore.type = WhereToPutType.LIST;
				whereToStore.positionInList = null;
				contextFragment.whereToMergeChildBean = whereToStore;

				// create transition with new editfragment
				EditConfigFragment.editNewConfigBean(altClassInstanceValues,
						ValueBindingHelper.toCharSequenceArray(altClassInstancesToDisplay), contextFragment, selectedRoom,
						roomConfig);
			}
		});

		// handle clicks in the list for edit
		listView.setOnItemClickListener(new OnItemClickListener() {

			// edit bean in new fragment if it was clicked
			@Override
			public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int position, long paramLong) {

				// remember position
				WhereToMergeBean whereToStore = new WhereToMergeBean();
				whereToStore.fieldName = field.getName();
				whereToStore.type = WhereToPutType.LIST;
				whereToStore.positionInList = position;
				contextFragment.whereToMergeChildBean = whereToStore;

				Serializable valueAtPosition = adapter.getItem(position);
				EditConfigFragment.editConfigBean(contextFragment, valueAtPosition, selectedRoom, roomConfig);
			}
		});

		// handle long clicks with context menu
		listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			List<Object> checkedItems = new ArrayList<Object>();


			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

				switch (item.getItemId()) {

				case R.id.menuEntryRemoveConfigurationClass:

					for (Object current : checkedItems) {
						listContent.remove(current);
					}
					adapter.notifyDataSetChanged();
					checkedItems.clear();

					// display create new button if list is empty
					if (adapter.isEmpty()) {
						createNew.setVisibility(View.VISIBLE);
					}

					// resize list to new size
					GuiUtils.setListViewHeightBasedOnChildren(listView);

					break;

				case R.id.menuEntryAddConfigurationClass:

					// replace object at position with a new one. therefore remember position in list
					WhereToMergeBean whereToStore = new WhereToMergeBean();
					whereToStore.fieldName = field.getName();
					whereToStore.type = WhereToPutType.LIST;
					whereToStore.positionInList = listContent.indexOf(checkedItems.get(0));
					contextFragment.whereToMergeChildBean = whereToStore;

					// create new transaction and edit new bean
					EditConfigFragment.editNewConfigBean(altClassInstanceValues,
							ValueBindingHelper.toCharSequenceArray(altClassInstancesToDisplay), contextFragment, selectedRoom,
							roomConfig);

					break;
				}

				mode.finish();
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
				// sync checked items to selections in the list
				if (checked) {
					checkedItems.add(listContent.get(position));
				} else {
					checkedItems.remove(position);
				}
				mode.setTitle(checkedItems.size() + " ausgewählt");

				// display "create new" only if there is one item clicked in the list
				MenuItem createItem = mode.getMenu().findItem(R.id.menuEntryAddConfigurationClass);
				if (checkedItems.size() == 1) {
					createItem.setVisible(true);
				} else {
					createItem.setVisible(false);
				}

			}
		});

		// resize to list size. we do not want to have local scrolling here.
		GuiUtils.setListViewHeightBasedOnChildren(listView);
	}
}
