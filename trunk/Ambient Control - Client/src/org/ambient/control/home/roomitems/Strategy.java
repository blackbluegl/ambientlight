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

import org.ambient.control.home.RoomFragment;
import org.ambientlight.room.entities.features.Entity;
import org.ambientlight.ws.Room;

import android.support.v4.app.Fragment;
import android.view.View;


/**
 * @author Florian Bornkessel
 * 
 */
public interface Strategy {

	/**
	 * will be called from ItemAdapter to get the visible and clickable content.
	 * 
	 * @param context
	 * @param entity
	 * @return
	 */
	public View onCreateView(Fragment context, Entity entity);


	/**
	 * handle onClick events for the given entity.
	 * 
	 * @param view
	 * @param room
	 * @param roomFragment
	 * @param entity
	 */
	public void onClick(View view, final Room room, final RoomFragment roomFragment, final Entity entity);


	/**
	 * handle onLongClick events for the given entity.
	 * 
	 * @param view
	 * @param room
	 * @param roomFragment
	 * @param entity
	 */
	public void onLongClick(View view, final Room room, final RoomFragment roomFragment, final Entity entity);
}
