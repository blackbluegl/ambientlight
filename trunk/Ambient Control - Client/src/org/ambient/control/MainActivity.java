package org.ambient.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ambient.control.climate.ClimateFragment;
import org.ambient.control.home.RoomFragment;
import org.ambient.control.nfc.NFCProgrammingFragment;
import org.ambient.control.processes.ProcessCardFragment;
import org.ambient.control.rest.RestClient;
import org.ambient.control.rest.URLUtils;
import org.ambient.roomservice.RoomConfigService;
import org.ambientlight.room.RoomConfiguration;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
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

	private List<IOnServiceConnectedListener> serviceListeners = new ArrayList<IOnServiceConnectedListener>();


	public void addServiceListener(IOnServiceConnectedListener listener) {
		serviceListeners.add(listener);
	}

	private RoomConfigService roomService;

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			roomService = ((RoomConfigService.MyBinder) binder).getService();
			for (IOnServiceConnectedListener listener : serviceListeners) {
				listener.onRoomServiceConnected(roomService);
			}
		}


		@Override
		public void onServiceDisconnected(ComponentName className) {
			roomService = null;
		}
	};

	private static final String LOG = "MainActivity";

	NFCProgrammingFragment nfcProgramming;
	ProcessCardFragment processCard;
	ClimateFragment climateFragment;

	ArrayList<String> fragments = new ArrayList<String>();
	String currentDialog = null;
	public LinearLayout content;


	public RoomConfigService getRoomConfigManager() {
		return roomService;
	}

	RestClient restClient;


	public RestClient getRestClient() {
		return restClient;

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mConnection);
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

		bindService(new Intent(this, RoomConfigService.class), mConnection, Context.BIND_AUTO_CREATE);

		setContentView(R.layout.activity_main);
		content = (LinearLayout) findViewById(R.id.LayoutMain);

		this.restClient = new RestClient();

		createNavigationDrawer(content);

		if (savedInstanceState == null) {
			createHomeFragment(content);
		} else {
			this.currentDialog = savedInstanceState.getString("currentDialog");
			this.fragments = savedInstanceState.getStringArrayList("fragments");
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
					createProcessCardFragment(content);
				}
				if (item.equals("Mein Klima")) {
					createClimateFragment(content);
				}
			}
		});
	}


	private void createProcessCardFragment(LinearLayout content) {
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


	public void createClimateFragment(LinearLayout content) {
		currentDialog = "Mein Klima";
		clearFragments(content);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		climateFragment = new ClimateFragment();

		ft.add(content.getId(), climateFragment, "climateFragmentTag");
		this.fragments.add("climateFragmentTag");
		ft.commit();
	}


	/**
	 * @param content
	 * @param ft
	 */
	public void createHomeFragment(LinearLayout content) {
		currentDialog = "Mein Ambiente";

		clearFragments(content);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Bundle argsRoom = new Bundle();
		argsRoom.putStringArrayList(RoomFragment.BUNDLE_SERVER_NAMES, getAllServers());

		RoomFragment roomFragment = new RoomFragment();
		roomFragment.setArguments(argsRoom);
		ft.add(content.getId(), roomFragment, "roomFragmentTag");
		this.fragments.add("roomFragmentTag");
		ft.commit();
	}


	/**
	 * @return
	 */
	private ArrayList<String> getAllServers() {
		return new ArrayList<String>(Arrays.asList(URLUtils.ANDROID_ADT_SERVERS));
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
