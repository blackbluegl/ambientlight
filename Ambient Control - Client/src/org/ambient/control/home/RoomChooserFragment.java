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
import org.ambient.control.RoomServiceAwareFragment;
import org.ambient.util.RoomUtil;
import org.ambientlight.ws.Room;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * display tab with rooms and higlight selected room.
 * 
 * @author Florian Bornkessel
 * 
 */
public class RoomChooserFragment extends RoomServiceAwareFragment {

	public static final String BUNDLE_SELECTED_ROOM_NAME = "selectedRoom";
	private String selectedRoom;

	private LinearLayout myContent;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			this.selectedRoom = savedInstanceState.getString(BUNDLE_SELECTED_ROOM_NAME);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myContent = (LinearLayout) inflater.inflate(R.layout.fragment_home_roomchooser, null);
		return myContent;
	}


	/*
	 * this is the entry point to get the selected room name the first time the application starts because the data layer comes up
	 * after onCreateView() is called.
	 * 
	 * @see org.ambient.control.RoomServiceAwareFragment#onResumeWithServiceConnected()
	 */
	@Override
	protected void onResumeWithService() {
		// get roomname from activity if it is not set in the fragment
		if (selectedRoom == null) {
			selectedRoom = ((HomeActivity) getActivity()).getSelecterRoom();
		}

		updateRoomContent();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.RoomServiceAwareFragment#onRoomConfigurationChange(java.lang.String, org.ambientlight.ws.Room)
	 */
	@Override
	public void onRoomConfigurationChange(String serverName, Room roomConfiguration) {
		if (isVisible() == false)
			return;
		updateRoomContent();
	}


	/**
	 * updates the content by creating dynamically all room items that a user may click on.
	 */
	private void updateRoomContent() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// clear all and build new on update
		myContent.removeAllViews();

		// if no room is available. stop here with an empty screen.
		if (selectedRoom == null)
			return;

		for (final String current : roomService.getAllRoomNames()) {
			final LinearLayout item = (LinearLayout) inflater.inflate(R.layout.layout_home_roomchooser_item, null);
			myContent.addView(item);
			item.setTag("item_" + current);

			TextView name = (TextView) item.findViewById(R.id.roomChooserItemTextView);
			name.setText(current);

			ImageView icon = (ImageView) item.findViewById(R.id.roomChooserItemImageView);

			if (RoomUtil.anySwitchTurnedOn(roomService.getRoomConfiguration(current))) {
				icon.setImageResource(R.drawable.ic_window_on);
			} else {
				icon.setImageResource(R.drawable.ic_window_off);
			}

			// create EventListener
			icon.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					((HomeActivity) getActivity()).setCurrentRoomByUser(current);
					setCurrentRoom(current);
				}
			});
		}

		setCurrentRoom(selectedRoom);
	}


	private void setCurrentRoom(String roomName) {

		for (int i = 0; i < myContent.getChildCount(); i++) {
			LinearLayout current = (LinearLayout) myContent.getChildAt(i);
			if (("item_" + roomName).equals(current.getTag())) {
				current.setBackgroundResource(R.color.roomChooserChoosen);
			} else {
				current.setBackgroundResource(R.color.roomChooserUnChoosen);
			}
		}
	}


	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putString(BUNDLE_SELECTED_ROOM_NAME, selectedRoom);
	}

}
