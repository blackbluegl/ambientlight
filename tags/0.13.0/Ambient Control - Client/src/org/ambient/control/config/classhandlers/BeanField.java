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
import java.util.List;

import org.ambient.control.R;
import org.ambient.control.config.ConfigBindingHelper;
import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambient.control.config.classhandlers.WhereToPutConfigurationData.WhereToPutType;
import org.ambientlight.room.RoomConfiguration;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * @author Florian Bornkessel
 * 
 */
public class BeanField {

	/**
	 * @param config
	 * @param field
	 * @param altValues
	 * @param altValuesToDisplay
	 * @param contentArea
	 * @throws IllegalAccessException
	 */
	public static void createView(final EditConfigHandlerFragment context, final Object config, final Field field,
			final List<String> altValues, final List<String> altValuesToDisplay, LinearLayout contentArea,
			final String selectedServer, final RoomConfiguration roomConfig) throws IllegalAccessException {

		WhereToPutConfigurationData whereToStore = new WhereToPutConfigurationData();
		whereToStore.fieldName = field.getName();
		whereToStore.type = WhereToPutType.FIELD;
		context.whereToPutDataFromChild = whereToStore;

		final TextView beanView = new TextView(contentArea.getContext());
		contentArea.addView(beanView);

		final Object fieldValue = field.get(config);

		if (fieldValue != null) {
			beanView.setText(fieldValue.getClass().getName());
		} else {
			beanView.setText("kein Objekt");
		}

		beanView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (fieldValue == null && altValuesToDisplay.size() > 0) {
					EditConfigHandlerFragment.createNewConfigBean(altValues,
							ConfigBindingHelper.toCharSequenceArray(altValuesToDisplay), context, selectedServer, roomConfig);
				} else if (fieldValue != null) {
					EditConfigHandlerFragment.editConfigBean(context, fieldValue, selectedServer, roomConfig);
				}
			}
		});

		beanView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				context.getActivity().startActionMode(new ActionMode.Callback() {

					@Override
					public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
						return false;
					}


					@Override
					public void onDestroyActionMode(ActionMode mode) {

					}


					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {
						MenuInflater inflater = mode.getMenuInflater();
						inflater.inflate(R.menu.fragment_edit_configuration_cab, menu);
						MenuItem editItem = mode.getMenu().findItem(R.id.menuEntryEditConfigurationClass);
						try {
							if (fieldValue != null) {
								editItem.setVisible(true);
							} else {
								editItem.setVisible(false);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						return true;
					}


					@Override
					public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

						switch (item.getItemId()) {

						case R.id.menuEntryRemoveConfigurationClass:

							try {
								field.set(config, null);
							} catch (Exception e) {
								// should not happen
							}
							beanView.setText("kein Objekt");

							break;

						case R.id.menuEntryEditConfigurationClass:

							EditConfigHandlerFragment.editConfigBean(context, fieldValue, selectedServer, roomConfig);

							break;

						}

						return false;
					}
				});
				return true;
			}
		});
	}

}
