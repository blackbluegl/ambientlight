package org.ambient.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ambient.control.home.RoofTopFragment;
import org.ambient.control.home.RoomFragment;
import org.ambient.control.rest.RestClient;
import org.ambient.control.rest.URLUtils;
import org.ambientlight.room.RoomConfiguration;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ListView;


public class MainActivity extends FragmentActivity {

	private static final String LOG = "MainActivity";

	RoofTopFragment roof;
	List<RoomFragment> rooms;
	RoomConfigAdapter roomConfigAdapter;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.roomConfigAdapter = this.getRoomConfigAdapter(this.getAllRoomServers());

		setContentView(R.layout.activity_main);

		DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		LinearLayout content = (LinearLayout) findViewById(R.id.LayoutMain);

		if (getSupportFragmentManager().findFragmentById(content.getId()) == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Bundle argsRoof = new Bundle();
			argsRoof.putStringArrayList(RoofTopFragment.BUNDLE_HOST_LIST, this.roomConfigAdapter.getServerNames());
			roof = new RoofTopFragment();
			roof.setArguments(argsRoof);
			ft.add(content.getId(), roof);

			for (String currentServer : roomConfigAdapter.roomConfigurations.keySet()) {
				Bundle argsRoom = new Bundle();

				argsRoom.putString(RoomFragment.BUNDLE_SERVER_NAME, currentServer);
				argsRoom.putParcelable(RoomFragment.BUNDLE_ROOM_CONFIG, roomConfigAdapter.getRoomConfigAsParceable(currentServer));

				RoomFragment roomFragment = new RoomFragment();
				roomFragment.setArguments(argsRoom);
				ft.add(content.getId(), roomFragment);
			}
			ft.commit();
		}
	}


	// TODO this here should discover real servers in future
	public ArrayList<String> getAllRoomServers() {
		ArrayList<String> result = new ArrayList<String>(Arrays.asList(URLUtils.ANDROID_ADT_SERVERS));
		return result;
	}


	public RoomConfigAdapter getRoomConfigAdapter(ArrayList<String> servers) {
		RoomConfigAdapter adapter = new RoomConfigAdapter();
		for (String currentServer : servers) {
			try {
				RoomConfiguration config = RestClient.getRoom(currentServer);
				adapter.roomConfigurations.put(currentServer, config);
			} catch (Exception e) {
				Log.e(LOG, "initRoomConfigurationAdapter: ommiting " + currentServer + " because of:", e);
			}
		}
		return adapter;
	}
}
