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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends FragmentActivity {

	private static final String LOG = "MainActivity";

	NFCProgrammingFragment nfcProgramming;

	List<RoomFragment> rooms;
	RoomConfigAdapter roomConfigAdapter;
	ArrayList<String> fragments = new ArrayList<String>();
	String currentDialog = null;


	public RoomConfigAdapter getRoomConfigAdapter() {
		return roomConfigAdapter;
	}

	RestClient restClient;


	public RestClient getRestClient() {
		return restClient;

	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("currentDialog", this.currentDialog);
		outState.putStringArrayList("fragments", this.fragments);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final LinearLayout content = (LinearLayout) findViewById(R.id.LayoutMain);

		this.roomConfigAdapter = this.createRoomConfigAdapter(this.getAllRoomServers());
		this.restClient = new RestClient(this.roomConfigAdapter);

		createNavigationDrawer(content);

		if (savedInstanceState == null) {
			createHomeFragment(content);
		} else {
			this.currentDialog = savedInstanceState.getString("currentDialog");
			this.fragments = savedInstanceState.getStringArrayList("fragments");
			if (this.currentDialog.equals("Mein Ambiente")) {
				createHomeFragment(content);
			}
		}

	}


	/**
	 * @param content
	 */
	public void createNavigationDrawer(final LinearLayout content) {
		DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		String[] values = new String[] { "Mein Ambiente", "Mein Klima", "NFC-Tag anlernen" };
		ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values));
		// // Set the list's click listener
		mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				final String item = (String) parent.getItemAtPosition(position);

				if (item.equals("Mein Ambiente")) {
					createHomeFragment(content);
				}
				if (item.equals("NFC-Tag anlernen")) {
					createNFCProgrammingFragment(content);
				}
			}
		});
	}


	public void createNFCProgrammingFragment(LinearLayout content) {
		currentDialog = "NFC-Tag anlernen";
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		for (String currentTag : fragments) {
			ft.remove(getSupportFragmentManager().findFragmentByTag(currentTag));
		}
		roomConfigAdapter.listeners.clear();
		fragments.clear();

		Bundle args = new Bundle();
		nfcProgramming = new NFCProgrammingFragment();
		nfcProgramming.setArguments(args);

		ft.add(content.getId(), nfcProgramming, "nfcProgrammingTag");
		this.fragments.add("nfcProgrammingTag");
		ft.commit();
	}


	/**
	 * @param content
	 * @param ft
	 */
	public void createHomeFragment(LinearLayout content) {
		currentDialog = "Mein Ambiente";
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		for (String currentTag : fragments) {
			ft.remove(getSupportFragmentManager().findFragmentByTag(currentTag));
		}
		fragments.clear();
		roomConfigAdapter.listeners.clear();
		roomConfigAdapter.metaListeners.clear();
		ft.commit();

		ft = getSupportFragmentManager().beginTransaction();

		Bundle argsRoof = new Bundle();
		argsRoof.putStringArrayList(RoofTopFragment.BUNDLE_HOST_LIST, this.roomConfigAdapter.getServerNames());
		RoofTopFragment roof = new RoofTopFragment();
		roof.setArguments(argsRoof);
		this.fragments.add("roof");
		this.roomConfigAdapter.addMetaListener(roof);
		ft.add(content.getId(), roof, "roof");
		ft.commit();

		for (String currentServer : roomConfigAdapter.getServerNames()) {
			ft = getSupportFragmentManager().beginTransaction();
			Bundle argsRoom = new Bundle();

			argsRoom.putString(RoomFragment.BUNDLE_SERVER_NAME, currentServer);
			argsRoom.putParcelable(RoomFragment.BUNDLE_ROOM_CONFIG, roomConfigAdapter.getRoomConfigAsParceable(currentServer));

			RoomFragment roomFragment = new RoomFragment();
			roomFragment.setArguments(argsRoom);
			this.fragments.add("roomFragment" + currentServer);
			this.roomConfigAdapter.addRoomConfigurationChangeListener(currentServer, roomFragment);
			ft.add(content.getId(), roomFragment, "roomFragment" + currentServer);
			ft.commit();
		}
	}


	@Override
	public void onPause() {
		super.onPause();
		// FragmentTransaction ft =
		// getSupportFragmentManager().beginTransaction();
		// for (Fragment current : this.fragments) {
		// ft.remove(current);
		// }
		// ft.commit();
		// fragments.clear();
		// roomConfigAdapter.listeners.clear();
		// roomConfigAdapter.metaListeners.clear();
	}


	// TODO this here should discover real servers in future
	public ArrayList<String> getAllRoomServers() {
		ArrayList<String> result = new ArrayList<String>(Arrays.asList(URLUtils.ANDROID_ADT_SERVERS));
		return result;
	}


	public RoomConfigAdapter createRoomConfigAdapter(ArrayList<String> servers) {
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
