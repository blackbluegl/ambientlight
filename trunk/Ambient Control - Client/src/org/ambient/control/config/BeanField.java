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

import org.ambient.control.R;
import org.ambient.control.config.WhereToMergeBean.WhereToPutType;
import org.ambientlight.ws.Room;

import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * creates an gui element with an clickable text. By clicking on the text the user may inspect underlying bean. if the field value
 * is null, the user can instantiate a new bean from the altClassValues list. By long clicking the user may decide to set the
 * field to null or to walk into the beans values. You have to annotate alternative values to have a useful setting.
 * 
 * @author Florian Bornkessel
 * 
 */
public class BeanField extends FieldGenerator {

	private static final String LOG = "BeanField";


	/**
	 * @param roomConfig
	 * @param bean
	 * @param field
	 * @param context
	 * @param contentArea
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 */
	public BeanField(Room roomConfig, Object bean, Field field, EditConfigFragment context, LinearLayout contentArea)
			throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		super(roomConfig, bean, field, context, contentArea);
	}


	/**
	 * 
	 * @param selectedRoom
	 * @throws IllegalAccessException
	 */
	public void createView(final String selectedRoom) throws IllegalAccessException {

		WhereToMergeBean whereToStore = new WhereToMergeBean();
		whereToStore.fieldName = field.getName();
		whereToStore.type = WhereToPutType.FIELD;
		contextFragment.whereToMergeChildBean = whereToStore;

		final TextView beanView = new TextView(contentArea.getContext());
		contentArea.addView(beanView);

		if (field.get(bean) != null) {
			beanView.setText(field.get(bean).getClass().getName());
		} else {
			beanView.setText("-");
		}

		beanView.setOnClickListener(new OnClickListener() {




			@Override
			public void onClick(View v) {

				if (getFieldValue() == null && altClassInstanceValues.size() > 0) {

					EditConfigFragment.editNewConfigBean(altClassInstanceValues,
							ValueBindingHelper.toCharSequenceArray(altClassInstancesToDisplay), contextFragment, selectedRoom,
							roomConfig);

				} else if (getFieldValue() == null && altClassInstancesToDisplay.size() == 0) {
					try {
						Log.i(LOG, "No alternative Values have been annotated to field or fieldtype class. Trying to"
								+ " instantiate a concrete Object of type: " + field.getType());

						Serializable newFieldValue = (Serializable) field.getType().newInstance();
						EditConfigFragment.editConfigBean(contextFragment, newFieldValue, selectedRoom, roomConfig);

					} catch (Exception e) {
						Log.e(LOG, "Could not instantiate object!", e);
						Toast.makeText(contentArea.getContext(),
								"No alternative Values annotated and class can not be instantiated directly", Toast.LENGTH_SHORT)
								.show();
					}

				} else if (getFieldValue() != null) {
					EditConfigFragment.editConfigBean(contextFragment, getFieldValue(), selectedRoom, roomConfig);
				}
			}
		});

		beanView.setOnLongClickListener(new OnLongClickListener() {



			@Override
			public boolean onLongClick(View v) {


				contextFragment.getActivity().startActionMode(new ActionMode.Callback() {

					@Override
					public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
						return false;
					}


					@Override
					public void onDestroyActionMode(ActionMode mode) {

					}


					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {

						// menu with edit and remove button
						MenuInflater inflater = mode.getMenuInflater();
						inflater.inflate(R.menu.fragment_edit_configuration_cab, menu);
						MenuItem editItem = mode.getMenu().findItem(R.id.menuEntryEditConfigurationClass);

						editItem.setVisible(getFieldValue() != null ? true : false);

						return true;
					}


					@Override
					public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

						switch (item.getItemId()) {

						case R.id.menuEntryRemoveConfigurationClass:

							try {
								field.set(bean, null);
							} catch (Exception e) {
								Log.e(LOG, "error, could not remove bean from field!", e);
								Toast.makeText(contentArea.getContext(), "could not remove bean!", Toast.LENGTH_SHORT).show();
							}

							beanView.setText("-");

							break;

						case R.id.menuEntryEditConfigurationClass:

							EditConfigFragment.editConfigBean(contextFragment, getFieldValue(), selectedRoom, roomConfig);

							break;

						}

						// if no item could handle let parent handle request
						return false;
					}
				});

				// signal that we handled the long click
				return true;
			}
		});
	}
}
