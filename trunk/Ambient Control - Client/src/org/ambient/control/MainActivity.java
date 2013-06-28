package org.ambient.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ambient.control.home.RoofTopFragment;
import org.ambient.control.home.RoomFragment;
import org.ambient.control.nfc.NFCProgrammingFragment;
import org.ambient.control.rest.RestClient;
import org.ambient.control.rest.URLUtils;
import org.ambientlight.room.RoomConfiguration;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MainActivity extends FragmentActivity {

	private static final String LOG = "MainActivity";
	// NfcAdapter mNfcAdapter;

	NFCProgrammingFragment nfcProgramming;
	RoofTopFragment roof;
	List<RoomFragment> rooms;
	RoomConfigAdapter roomConfigAdapter;
	List<Fragment> fragments = new ArrayList<Fragment>();


	public RoomConfigAdapter getRoomConfigAdapter() {
		return roomConfigAdapter;
	}

	RestClient restClient;


	public RestClient getRestClient() {
		return restClient;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.v("testTag", "started");
		// mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		// if (mNfcAdapter == null) {
		// Toast.makeText(this, "NFC is not available",
		// Toast.LENGTH_LONG).show();
		// } else {
		// Log.v("testTag", "register callback");
		// mNfcAdapter.setNdefPushMessageCallback(this, this);
		// }
		// Register callback TODO Only if we want to write to a tag

		this.roomConfigAdapter = this.getRoomConfigAdapter(this.getAllRoomServers());
		this.restClient = new RestClient(this.roomConfigAdapter);

		setContentView(R.layout.activity_main);

		DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		// ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
		// // Set the adapter for the list view
		// mDrawerList.setAdapter(new ArrayAdapter<String>(this,
		// R.layout.drawer_list_item, mPlanetTitles));
		// // Set the list's click listener
		// mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		LinearLayout content = (LinearLayout) findViewById(R.id.LayoutMain);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		// createHomeFragment(content, ft);
		createNFCProgrammingFragment(content, ft);
		ft.commit();
	}


	public void createNFCProgrammingFragment(LinearLayout content, FragmentTransaction ft) {
		Bundle args = new Bundle();
		nfcProgramming = new NFCProgrammingFragment();
		nfcProgramming.setArguments(args);
		ft.replace(content.getId(), nfcProgramming);
	}


	/**
	 * @param content
	 * @param ft
	 */
	public void createHomeFragment(LinearLayout content, FragmentTransaction ft) {
		Bundle argsRoof = new Bundle();
		argsRoof.putStringArrayList(RoofTopFragment.BUNDLE_HOST_LIST, this.roomConfigAdapter.getServerNames());
		roof = new RoofTopFragment();
		roof.setArguments(argsRoof);
		this.fragments.add(roof);
		this.roomConfigAdapter.addMetaListener(roof);
		ft.replace(content.getId(), roof);

		for (String currentServer : roomConfigAdapter.getServerNames()) {
			Bundle argsRoom = new Bundle();

			argsRoom.putString(RoomFragment.BUNDLE_SERVER_NAME, currentServer);
			argsRoom.putParcelable(RoomFragment.BUNDLE_ROOM_CONFIG, roomConfigAdapter.getRoomConfigAsParceable(currentServer));

			RoomFragment roomFragment = new RoomFragment();
			roomFragment.setArguments(argsRoom);
			this.fragments.add(roomFragment);
			this.roomConfigAdapter.addRoomConfigurationChangeListener(currentServer, roomFragment);
			ft.add(content.getId(), roomFragment);
		}
	}


	@Override
	public void onPause() {
		super.onPause();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		for (Fragment current : this.fragments) {
			ft.remove(current);
		}
		ft.commit();
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
				adapter.addRoomConfiguration(currentServer, config);
			} catch (Exception e) {
				Log.e(LOG, "initRoomConfigurationAdapter: ommiting " + currentServer + " because of:", e);
			}
		}
		return adapter;
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleNFCIntent(intent);
	}


	/**
	 * @param intent
	 */
	private void handleNFCIntent(Intent intent) {

		Tag mytag = null;
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Toast.makeText(this, "Tag erkannt", Toast.LENGTH_SHORT).show();
		}

		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Toast.makeText(this, "Tag erkannt", Toast.LENGTH_SHORT).show();
		}
		if (nfcProgramming != null) {
			nfcProgramming.mytag = mytag;
		}
	}

}
