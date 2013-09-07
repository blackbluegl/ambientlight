package org.ambient.control;

import java.util.ArrayList;
import java.util.Arrays;

import org.ambient.control.home.RoomFragment;
import org.ambient.control.nfc.NFCProgrammingFragment;
import org.ambient.control.processes.ProcessCardFragment;
import org.ambient.control.rest.RestClient;
import org.ambient.control.rest.URLUtils;
import org.ambientlight.room.RoomConfiguration;

import android.app.ActionBar;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends FragmentActivity {

	private static final String LOG = "MainActivity";

	NFCProgrammingFragment nfcProgramming;
	ProcessCardFragment processCard;

	RoomConfigManager roomConfigManager;
	ArrayList<String> fragments = new ArrayList<String>();
	String currentDialog = null;
	public LinearLayout content;


	public RoomConfigManager getRoomConfigManager() {
		return roomConfigManager;
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


	public void replaceFragment(Fragment frag, String tag, String dialogName) {
		clearFragments(content);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		ft.replace(content.getId(), frag, tag);
		this.fragments.add(tag);
		this.currentDialog = dialogName;
		ft.addToBackStack(null);
		ft.commit();
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		content = (LinearLayout) findViewById(R.id.LayoutMain);

		this.roomConfigManager = this.createRoomConfigAdapter(this.getAllRoomServers());
		this.restClient = new RestClient(this.roomConfigManager);

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

		ActionBar actionBar = getActionBar();
		actionBar.show();
	}


	public void createNavigationDrawer(final LinearLayout content) {
		DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		String[] values = new String[] { "Mein Ambiente", "Mein Klima", "Meine Prozesse", "NFC-Tag anlernen" };
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
				if (item.equals("Meine Prozesse")) {
					createProcessCard(content);
				}
			}
		});
	}


	private void createProcessCard(LinearLayout content) {
		currentDialog = "Meine Prozesse";
		clearFragments(content);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		processCard = new ProcessCardFragment();
		ft.add(content.getId(), processCard, "processCardTag");
		this.fragments.add("processCardTag");
		ft.commit();
	}


	public void createNFCProgrammingFragment(LinearLayout content) {
		currentDialog = "NFC-Tag anlernen";
		clearFragments(content);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		nfcProgramming = new NFCProgrammingFragment();

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

		clearFragments(content);
		roomConfigManager.removeAllListeners();

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Bundle argsRoom = new Bundle();
		argsRoom.putStringArrayList(RoomFragment.BUNDLE_SERVER_NAMES, this.roomConfigManager.getServerNames());

		RoomFragment roomFragment = new RoomFragment();
		roomFragment.setArguments(argsRoom);
		this.fragments.add("roomFragment");
		for (String currentServer : this.getAllRoomServers()) {
			this.roomConfigManager.addRoomConfigurationChangeListener(currentServer, roomFragment);
		}
		ft.add(content.getId(), roomFragment, "roomFragmentTag");
		this.fragments.add("roomFragmentTag");
		ft.commit();
	}


	/**
	 * 
	 */
	public void clearFragments(LinearLayout content) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		for (String currentTag : fragments) {
			ft.remove(getSupportFragmentManager().findFragmentByTag(currentTag));
		}
		fragments.clear();
		ft.commit();

		content.removeAllViews();
	}


	// TODO this here should discover real servers in future
	public ArrayList<String> getAllRoomServers() {
		ArrayList<String> result = new ArrayList<String>(Arrays.asList(URLUtils.ANDROID_ADT_SERVERS));
		return result;
	}


	public RoomConfigManager createRoomConfigAdapter(ArrayList<String> servers) {
		RoomConfigManager adapter = new RoomConfigManager();
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
