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

import java.io.Serializable;

import org.ambient.control.R;
import org.ambient.control.config.EditConfigActivity;
import org.ambient.rest.RestClient;
import org.ambient.util.GuiUtils;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.ws.Room;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;


/**
 * creates an activity to edit RenderingProgramConfigurations of Renderables. It provides a preview option and handles the save
 * action on the server.
 * 
 * @author Florian Bornkessel
 * 
 */
public class EditRenderingConfigActivity extends EditConfigActivity {

	private static final String EXTRA_ENTITY_ID = "entityId";
	private static final String EXTRA_ORIGINAL_CONFIG = "originalConfig";


	public static void createInstanceForNewObject(Activity caller,
			RenderingProgramConfiguration renderingProgrammConfigClassNameToEdit, EntityId itemId, String roomName, Room room) {

		// we need the entity id and the original config value for save and restore actions
		Bundle bundle = new Bundle();
		bundle.putSerializable(EXTRA_ENTITY_ID, itemId);
		bundle.putSerializable(EXTRA_ORIGINAL_CONFIG,
				(Serializable) GuiUtils.deepCloneSerializeable(renderingProgrammConfigClassNameToEdit));

		createInstance(EditRenderingConfigActivity.class, bundle, caller, true, RenderingProgramConfiguration.class,
				null, roomName, room);
	}


	public static void createInstanceForEditObject(Activity caller,
			RenderingProgramConfiguration renderingProgrammConfigClassNameToEdit, EntityId itemId, String roomName, Room room) {

		// we need the entity id and the original config value for save and restore actions
		Bundle bundle = new Bundle();
		bundle.putSerializable(EXTRA_ENTITY_ID, itemId);
		bundle.putSerializable(EXTRA_ORIGINAL_CONFIG,
				(Serializable) GuiUtils.deepCloneSerializeable(renderingProgrammConfigClassNameToEdit));

		createInstance(EditRenderingConfigActivity.class, bundle, caller, false, null, renderingProgrammConfigClassNameToEdit,
				roomName, room);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// preview button will write config to the server. if the user presses the back button the original config will be saved
		// to the server
		super.onCreateOptionsMenu(menu);
		MenuItem previewItem = menu.add("Vorschau");
		previewItem.setIcon(R.drawable.ic_preview);
		previewItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		previewItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				writeConfig((RenderingProgramConfiguration) getValueToEdit());
				return true;
			}
		});

		return true;
	}


	/**
	 * method to handle the users check button action. Writes config to server
	 * 
	 * @see org.ambient.control.config.EditConfigOnExitListener#onIntegrateConfiguration(java.lang.String, java.lang.Object)
	 */
	@Override
	public void onIntegrateConfiguration(String roomName, Object configuration) {

		writeConfig((RenderingProgramConfiguration) getIntent().getSerializableExtra(EXTRA_EDIT_VALUE));

		setResult(Activity.RESULT_OK);
		finish();
	}


	/**
	 * user cancled the action. Get shure and restore the original value to the server.
	 * 
	 * @see org.ambient.control.config.EditConfigOnExitListener#onRevertConfiguration(java.lang.String, java.lang.Object)
	 */
	@Override
	public void onRevertConfiguration(String roomName, Object configuration) {

		writeConfig((RenderingProgramConfiguration) getValueToEdit());

		setResult(Activity.RESULT_CANCELED);
		finish();
	}


	private void writeConfig(RenderingProgramConfiguration config) {
		String roomName = getIntent().getStringExtra(EXTRA_ROOM_NAME);
		EntityId id = (EntityId) getIntent().getExtras().getSerializable(EXTRA_ENTITY_ID);

		RestClient.setRenderingConfiguration(roomName, id, config);
	}

}
