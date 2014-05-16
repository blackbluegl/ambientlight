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

package org.ambient.control.home;

import org.ambient.control.R;
import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambient.rest.RestClient;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.ws.Room;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;


/**
 * @author Florian Bornkessel
 * 
 */
public class ActorConductEditFragment extends EditConfigHandlerFragment {

	final public static String ITEM_ID = "itemId";
	EntityId itemId = null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		itemId = (EntityId) getArguments().getSerializable(ITEM_ID);
		return super.onCreateView(inflater, container, savedInstanceState);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem previewItem = menu.add("Vorschau");
		previewItem.setIcon(R.drawable.ic_preview);
		previewItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		previewItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				RestClient.setRenderingConfiguration(selectedRoom, itemId, (RenderingProgramConfiguration) myConfigurationData);
				return true;
			}
		});
	}


	public static <T> Bundle createNewConfigBean(Class<T> clazz, final Fragment fragment, final String roomName,
			final Room roomConfiguration, final EntityId itemName) throws ClassNotFoundException,
			java.lang.InstantiationException, IllegalAccessException {

		Bundle args = EditConfigHandlerFragment.createNewConfigBean(clazz, fragment, roomName, roomConfiguration);
		args.putSerializable(ITEM_ID, itemName);
		return args;
	}


	protected static EditConfigHandlerFragment createInstance() {
		return new ActorConductEditFragment();
	}

}
