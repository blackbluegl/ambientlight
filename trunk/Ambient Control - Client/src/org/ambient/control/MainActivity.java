package org.ambient.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ambient.control.climate.ClimateFragment;
import org.ambient.control.home.RoomFragment;
import org.ambient.control.nfc.NFCProgrammingFragment;
import org.ambient.control.processes.ProcessCardFragment;
import org.ambient.rest.Rest;
import org.ambient.roomservice.RoomConfigService;
import org.ambientlight.ws.Room;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

	private static final String LOG = "MainActivity";

	Fragment currentFragment = null;

	String currentDialog = null;
	public LinearLayout content;

	private List<IRoomServiceCallbackListener> roomServiceListeners = new ArrayList<IRoomServiceCallbackListener>();

	private RoomConfigService roomService;

	private ServiceConnection roomServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			roomService = ((RoomConfigService.MyBinder) binder).getService();
			for (IRoomServiceCallbackListener listener : roomServiceListeners) {
				listener.onRoomServiceConnected(roomService);
			}
		}


		@Override
		public void onServiceDisconnected(ComponentName className) {
			roomService = null;
		}
	};

	private final BroadcastReceiver roomServiceUpdateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (action.equals(RoomConfigService.BROADCAST_INTENT_UPDATE_ROOMCONFIG)) {
				String serverName = intent.getExtras().getString(RoomConfigService.EXTRA_SERVERNAME);
				Room config = (Room) intent.getExtras().getSerializable(
						RoomConfigService.EXTRA_ROOMCONFIG);
				Log.i(LOG, "got update for Room");
				for (IRoomServiceCallbackListener listener : roomServiceListeners) {
					listener.onRoomConfigurationChange(serverName, config);
				}
			}
		}
	};


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("currentDialog", this.currentDialog);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(roomServiceConnection);
		unregisterReceiver(roomServiceUpdateReceiver);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// create a handle to the service that we never close. so the service
		// will stay alive even on recreate of this activity
		startService(new Intent(this, RoomConfigService.class));

		registerReceiver(roomServiceUpdateReceiver, new IntentFilter(RoomConfigService.BROADCAST_INTENT_UPDATE_ROOMCONFIG));
		bindService(new Intent(this, RoomConfigService.class), roomServiceConnection, Context.BIND_AUTO_CREATE);

		setContentView(R.layout.activity_main);
		content = (LinearLayout) findViewById(R.id.LayoutMain);

		createNavigationDrawer(content);

		if (savedInstanceState == null) {
			createHomeFragment(content);
		} else {
			this.currentDialog = savedInstanceState.getString("currentDialog");
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
					currentFragment = createHomeFragment(content);
				}
				if (item.equals("NFC-Tag anlernen")) {
					currentFragment = createNFCProgrammingFragment(content);
				}
				if (item.equals("Meine Prozesse")) {
					currentFragment = createProcessCardFragment(content);
				}
				if (item.equals("Mein Klima")) {
					currentFragment = createClimateFragment(content);
				}

				if (currentFragment instanceof IRoomServiceCallbackListener) {
					if (roomService != null) {
						((IRoomServiceCallbackListener) currentFragment).setRoomService(roomService);
					}
				}
			}
		});
	}


	private Fragment createProcessCardFragment(LinearLayout content) {
		currentDialog = "Meine Prozesse";
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		ProcessCardFragment processCard = new ProcessCardFragment();
		ft.replace(content.getId(), processCard, "processCardTag");
		ft.commit();
		return processCard;
	}


	public Fragment createNFCProgrammingFragment(LinearLayout content) {
		currentDialog = "NFC-Tag anlernen";

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		NFCProgrammingFragment nfcProgramming = new NFCProgrammingFragment();
		ft.replace(content.getId(), nfcProgramming, "nfcProgrammingTag");
		ft.commit();
		return nfcProgramming;
	}


	public Fragment createClimateFragment(LinearLayout content) {
		currentDialog = "Mein Klima";

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		ClimateFragment climateFragment = new ClimateFragment();
		ft.replace(content.getId(), climateFragment, "climateFragmentTag");
		ft.commit();
		return climateFragment;
	}


	/**
	 * @param content
	 * @param ft
	 */
	public Fragment createHomeFragment(LinearLayout content) {
		currentDialog = "Mein Ambiente";

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Bundle argsRoom = new Bundle();
		argsRoom.putStringArrayList(RoomFragment.BUNDLE_SERVER_NAMES, getAllServers());

		RoomFragment roomFragment = new RoomFragment();
		roomFragment.setArguments(argsRoom);
		ft.replace(content.getId(), roomFragment, "roomFragmentTag");
		ft.commit();
		return roomFragment;
	}


	/**
	 * @return
	 */
	private ArrayList<String> getAllServers() {
		return new ArrayList<String>(Arrays.asList(Rest.ANDROID_ADT_SERVERS));
	}


	// TODO this here should discover real servers in future
	public ArrayList<String> getAllRoomServers() {
		ArrayList<String> result = new ArrayList<String>(Arrays.asList(Rest.ANDROID_ADT_SERVERS));
		return result;
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

		if (currentFragment != null && currentFragment instanceof NFCProgrammingFragment) {
			((NFCProgrammingFragment) currentFragment).mytag = mytag;
		}
	}


	public void replaceFragment(Fragment frag, String tag, String dialogName) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		ft.replace(content.getId(), frag, tag);
		this.currentDialog = dialogName;
		ft.addToBackStack(null);

		ft.commit();
	}


	public void addServiceListener(IRoomServiceCallbackListener listener) {
		roomServiceListeners.add(listener);
	}
}
