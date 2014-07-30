package org.ambient.control.home;

import org.ambient.control.R;
import org.ambient.control.navigation.NavigationActivity;
import org.ambientlight.ws.Room;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.LinearLayout;


/**
 * handles home screen with rooms and items in the rooms.
 * 
 * @author Florian Bornkessel
 * 
 */
public class HomeActivity extends NavigationActivity {

	// holds a reference to decide which room will be displayed by the RoomFragment
	public static final String BUNDLE_SELECTED_ROOM = "selectedRoom";
	private String selectedRoom;


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			selectedRoom = savedInstanceState.getString(BUNDLE_SELECTED_ROOM);

		} else {
			LinearLayout content = (LinearLayout) findViewById(R.id.navActionContentLinearLayout);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

			RoomChooserFragment roomChooserFragment = new RoomChooserFragment();
			ft.add(content.getId(), roomChooserFragment);

			RoomFragment roomFragment = new RoomFragment();
			ft.add(content.getId(), roomFragment, "roomFragment");
			// ft.addToBackStack(null);

			ft.commit();
		}

	}


	@Override
	protected void onRoomConfigurationChange(String roomName, Room config) {
		// if ambientcontrol started without server connection and now reaches the server again, check if a roomname was set
		// earlier. If not, use the first name that is available.
		if (selectedRoom == null && roomService.getAllRoomNames().isEmpty() == false) {
			selectedRoom = roomService.getAllRoomNames().iterator().next();
		}
	}


	public String getSelecterRoom() {
		return selectedRoom;
	}


	/**
	 * method for the RoomChooserFragment to reflect the chosen rooms by user.
	 * 
	 * @param current
	 */
	public void setCurrentRoomByUser(String current) {
		this.selectedRoom = current;
		RoomFragment roomFragment = (RoomFragment) getSupportFragmentManager().findFragmentByTag("roomFragment");
		roomFragment.roomName = current;
		roomFragment.updateRoomContent();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.RoomServiceAwareActivity#onRoomServiceConnected()
	 */
	@Override
	protected void onRoomServiceConnected() {
		// at first start we have no room set. we use the first room that we find
		if (selectedRoom == null && roomService.getAllRoomNames().isEmpty() == false) {
			selectedRoom = roomService.getAllRoomNames().iterator().next();
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(BUNDLE_SELECTED_ROOM, selectedRoom);
	}
}
