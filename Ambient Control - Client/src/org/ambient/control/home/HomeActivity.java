package org.ambient.control.home;

import java.util.ArrayList;

import org.ambient.control.R;
import org.ambient.control.RoomServiceAwareActivity;
import org.ambient.rest.RestClient;

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


public class HomeActivity extends RoomServiceAwareActivity {

	RoomFragment roomFragment;
	RoomChooserFragment roomChooserFragment;

	public static final String BUNDLE_SELECTED_ROOM = "selectedRoom";
	String selectedRoom;

	LinearLayout content;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			selectedRoom = savedInstanceState.getString(BUNDLE_SELECTED_ROOM);
		} else {
			selectedRoom = getAllRoomNames().get(0);
		}

		setContentView(R.layout.activity_main);

		content = (LinearLayout) findViewById(R.id.homeMainLinearLayout);

		createNavigationDrawer();

		ActionBar actionBar = getActionBar();
		actionBar.show();

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		roomChooserFragment = new RoomChooserFragment();
		Bundle argsRoomChooser = new Bundle();
		argsRoomChooser.putString(RoomChooserFragment.BUNDLE_SELECTED_ROOM_NAME, selectedRoom);
		roomChooserFragment.setArguments(argsRoomChooser);
		ft.add(roomChooserFragment, null);

		roomFragment = new RoomFragment();
		Bundle argsRoom = new Bundle();
		argsRoom.putString(RoomFragment.BUNDLE_ROOM_NAME, selectedRoom);
		roomFragment.setArguments(argsRoom);
		ft.add(content.getId(), roomFragment);

		ft.commit();

	}


	public void createNavigationDrawer() {
		DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		String[] values = new String[] { "Mein Ambiente", "Mein Klima", "Meine Prozesse", "NFC-Tag anlernen" };
		ListView mDrawerList = (ListView) findViewById(R.id.homeLeftDrawer);
		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values));
		// // Set the list's click listener
		mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				final String item = (String) parent.getItemAtPosition(position);
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


	public ArrayList<String> getAllRoomNames() {
		try {
			if (roomService != null)
				return new ArrayList<String>(roomService.getAllRoomNames());
			return new ArrayList<String>(RestClient.getRoomNames());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * @param current
	 */
	public void setRoomActive(String current) {
		this.selectedRoom = current;
	}
}
