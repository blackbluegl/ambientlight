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

package org.ambient.control.home.roomitems;

import org.ambient.control.R;
import org.ambient.control.home.EditRenderingConfigActivity;
import org.ambient.control.home.RoomFragment;
import org.ambient.rest.RestClient;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.room.entities.features.Entity;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.lightobject.Renderable;
import org.ambientlight.ws.Room;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


/**
 * item strategy to handle lightobjects. clicks will turn off/on the light objects. LongClicks will change the
 * renderingConfiguration.
 * 
 * @author Florian Bornkessel
 */
public abstract class LightObjectStrategy implements Strategy {

	@Override
	public View onCreateView(Fragment context, Entity entity) {

		LayoutInflater inflater = (LayoutInflater) context.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout container = (RelativeLayout) inflater.inflate(R.layout.layout_room_item_lightobject_content, null);
		ImageView itemContent = (ImageView) container.findViewById(R.id.imageViewItemIcon);
		updateIcon(((Switchable) entity).getPowerState(), itemContent);

		return container;
	}


	protected abstract int getActiveIcon();


	protected abstract int getDisabledIcon();


	protected void updateIcon(boolean isActive, ImageView itemContent) {

		if (isActive) {
			itemContent.setImageResource(this.getActiveIcon());
		} else {
			itemContent.setImageResource(this.getDisabledIcon());
		}
	}


	@Override
	public void onClick(View view, final Room room, final RoomFragment roomFragment, final Entity entity) {

		try {
			// update the icon directly and do not wait for response cycle - we prevent that the user experiences a gap (request
			// to server, response to callback)
			updateIcon(!((Switchable) entity).getPowerState(), (ImageView) view.findViewById(R.id.imageViewItemIcon));

			RestClient.setSwitchablePowerState(roomFragment.roomName, entity.getId(), !((Switchable) entity).getPowerState());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * edit the renderingconfiguration of the light object. preview, and save actions are handled in the editActivity.
	 */
	@Override
	public void onLongClick(View view, final Room room, final RoomFragment roomFragment, final Entity entity) {

		final RenderingProgramConfiguration itemConfig = ((Renderable) entity).getRenderingProgrammConfiguration();

		roomFragment.getActivity().startActionMode(new ActionMode.Callback() {

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

				switch (item.getItemId()) {

				case R.id.menuEntryEditActorConduct:

					EditRenderingConfigActivity.createInstanceForEditObject(roomFragment.getActivity(), itemConfig,
							entity.getId(), room.roomName, room);
					break;

				case R.id.menuEntryAddActorConduct:

					EditRenderingConfigActivity.createInstanceForNewObject(roomFragment.getActivity(), itemConfig,
							entity.getId(), room.roomName, room);
					break;

				}
				mode.finish();
				return true;
			}


			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.fragment_room_cab, menu);
				return true;
			}


			@Override
			public void onDestroyActionMode(ActionMode mode) {
			}


			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}
		});
	}

}
