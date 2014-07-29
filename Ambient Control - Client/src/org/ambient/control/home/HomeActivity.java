package org.ambient.control.home;

import org.ambient.control.R;
import org.ambient.control.RoomServiceAwareActivity;
import org.ambientlight.ws.Room;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;


/**
 * handles home screen with rooms and items in the rooms.
 * 
 * @author Florian Bornkessel
 * 
 */
public class HomeActivity extends RoomServiceAwareActivity {

	// holds a reference to decide which room will be displayed by the RoomFragment
	public static final String BUNDLE_SELECTED_ROOM = "selectedRoom";
	private String selectedRoom;


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		ActionBar actionBar = getActionBar();
		actionBar.show();

		if (savedInstanceState != null) {
			selectedRoom = savedInstanceState.getString(BUNDLE_SELECTED_ROOM);

		} else {
			LinearLayout content = (LinearLayout) findViewById(R.id.homeMainLinearLayout);

			createNavigationDrawer();

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

			RoomChooserFragment roomChooserFragment = new RoomChooserFragment();
			ft.add(content.getId(), roomChooserFragment);

			RoomFragment roomFragment = new RoomFragment();
			ft.add(content.getId(), roomFragment, "roomFragment");
			ft.addToBackStack(null);

			ft.commit();
		}

	}


	private void createNavigationDrawer() {
		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		String[] values = new String[] { "Mein Ambiente", "Mein Klima", "Meine Prozesse", "NFC-Tag anlernen" };
		ListView drawerListView = (ListView) findViewById(R.id.homeLeftDrawer);

		drawerListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values));
		// // Set the list's click listener
		drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				final String navigation = (String) parent.getItemAtPosition(position);
				//
				// if (item.equals("Mein Ambiente")) {
				// currentFragment = createHomeFragment(content);
				// }
				// if (item.equals("NFC-Tag anlernen")) {
				// currentFragment = createNFCProgrammingFragment(content);
				// }
				// if (item.equals("Meine Prozesse")) {
				// currentFragment = createProcessCardFragment(content);
				// }
				// if (item.equals("Mein Klima")) {
				// currentFragment = createClimateFragment(content);
				// }
				//
				// if (currentFragment instanceof IRoomServiceCallbackListener) {
				// if (roomService != null) {
				// ((IRoomServiceCallbackListener) currentFragment).setRoomService(roomService);
				// }
				// }
			}
		});
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
