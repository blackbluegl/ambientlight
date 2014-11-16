package org.ambient.control.home;

import java.util.Collection;

import org.ambient.control.R;
import org.ambient.control.navigation.NavigationActivity;
import org.ambient.rest.RestClient;
import org.ambientlight.config.room.entities.climate.TemperaturMode;
import org.ambientlight.room.entities.climate.util.MaxThermostateMode;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.ws.Room;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;


/**
 * handles home screen with rooms and items in the rooms.
 * 
 * @author Florian Bornkessel
 * 
 */
public class HomeActivity extends NavigationActivity {

	public static String LOG = HomeActivity.class.getName();

	// holds a reference to decide which room will be displayed by the RoomFragment
	public static final String BUNDLE_SELECTED_ROOM = "selectedRoom";
	private String selectedRoom;

	private Menu menu;


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			selectedRoom = savedInstanceState.getString(BUNDLE_SELECTED_ROOM);
		} else {
			LinearLayout content = (LinearLayout) findViewById(R.id.navActionContentLinearLayout);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

			RoomChooserFragment roomChooserFragment = new RoomChooserFragment();
			ft.add(content.getId(), roomChooserFragment, "roomChooserFragment");

			RoomFragment roomFragment = new RoomFragment();
			ft.add(content.getId(), roomFragment, "roomFragment");
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

		if (this.menu != null) {
			updateMenuState(roomService.getAllRoomConfigurations());
		}
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


	public String getSelecterRoom() {
		return selectedRoom;
	}


	/**
	 * method for the RoomChooserFragment to reflect the chosen rooms by user.
	 * 
	 * @param current
	 */
	public void setSelectedRoomByUser(String current) {
		this.selectedRoom = current;
		RoomFragment roomFragment = (RoomFragment) getSupportFragmentManager().findFragmentByTag("roomFragment");
		roomFragment.roomName = current;
		roomFragment.updateRoomContent();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		this.menu = menu;
		getMenuInflater().inflate(R.menu.activity_home, menu);

		this.updateMenuState(roomService.getAllRoomConfigurations());

		return true;
	}


	/**
	 * controls visibility of the menu items according to the room configuration
	 * 
	 * @param rooms
	 */
	private void updateMenuState(Collection<Room> rooms) {

		// disable all buttons if no room exists
		if (rooms == null || rooms.isEmpty()) {
			menu.findItem(R.id.menu_power).setVisible(false);
			menu.findItem(R.id.menu_heating).setVisible(false);
			return;
		}

		// enable global power down switch
		boolean powerSwitchEnable = false;
		for (Room currentRoom : rooms) {
			if (currentRoom.switchesManager == null || currentRoom.switchesManager.switches == null) {
				continue;
			}

			Switchable currentMainSwitch = currentRoom.switchesManager.switches.get(new EntityId(
					EntityId.DOMAIN_SWITCH_VIRTUAL_MAIN, EntityId.ID_SWITCH_VIRTUAL_MAIN_SWITCH));
			if (currentMainSwitch != null) {
				powerSwitchEnable = true;
				break;
			}
		}
		menu.findItem(R.id.menu_power).setVisible(powerSwitchEnable);

		// update climate button
		Boolean climateEco = areAllClimateStatesInEco();
		if (climateEco == null) {
			menu.findItem(R.id.menu_heating).setVisible(false);
		} else if (climateEco) {
			menu.findItem(R.id.menu_heating).setIcon(R.drawable.ic_heating_all_auto);
			menu.findItem(R.id.menu_heating).setVisible(true);
		} else {
			menu.findItem(R.id.menu_heating).setIcon(R.drawable.ic_heating_all_eco);
			menu.findItem(R.id.menu_heating).setVisible(true);
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.menu_power) {
			powerOffAllMainSwitches();
			return true;
		}
		if (id == R.id.menu_heating) {
			if (areAllClimateStatesInEco() == null) {
				item.setVisible(false);
			} else if (areAllClimateStatesInEco()) {
				item.setIcon(R.drawable.ic_action_refresh);
				changeAllClimateStatesToAuto(true);
			} else {
				item.setIcon(R.drawable.ic_action_refresh);
				changeAllClimateStatesToAuto(false);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	/**
	 * @param b
	 *            true will set the climateManager to auto mode, false to eco.
	 */
	private void changeAllClimateStatesToAuto(boolean autoMode) {
		if (autoMode) {
			Toast.makeText(this, "alle Heizungen Auto", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "alle Heizungen Eco", Toast.LENGTH_SHORT).show();
		}

		for (Room current : this.roomService.getAllRoomConfigurations()) {
			if (current.climateManager == null) {
				continue;
			}
			if (autoMode) {
				try {
					RestClient.setTemperatureMode(current.roomName, new TemperaturMode(0.0f, null, MaxThermostateMode.AUTO));
				} catch (Exception e) {
					Toast.makeText(this, "Could not set mode to Auto for Room: " + current.roomName, Toast.LENGTH_LONG).show();
					Log.e(LOG, "Could not set mode to Auto for Room: " + current.roomName, e);
				}
			} else {
				try {
					RestClient.setTemperatureMode(current.roomName, new TemperaturMode(current.climateManager.ecoTemperatur,
							null, MaxThermostateMode.MANUAL));
				} catch (Exception e) {
					Toast.makeText(this, "Could not set mode to Auto for Room: " + current.roomName, Toast.LENGTH_LONG).show();
					Log.e(LOG, "Could not set mode to Auto for Room: " + current.roomName, e);
				}
			}
		}

	}


	/**
	 * @return true if all rooms are in eco mode. false if at least one is not. null if no climatemanager is available.
	 */
	private Boolean areAllClimateStatesInEco() {

		// if no climatemanager will be found we return null later
		boolean anyClimateManagerFound = false;

		for (Room current : this.roomService.getAllRoomConfigurations()) {
			if (current.climateManager != null) {
				anyClimateManagerFound = true;
			} else {
				continue;
			}

			// if any climate differs from eco return false
			if (current.climateManager.mode != MaxThermostateMode.MANUAL
					|| current.climateManager.temperature > current.climateManager.ecoTemperatur)
				return false;
		}

		// all climatemanagers have the same state
		if (anyClimateManagerFound)
			return true;
		else
			return null;
	}


	/**
	 * power off main switches in all rooms.
	 */
	private void powerOffAllMainSwitches() {

		Toast.makeText(this, "alle Schalter aus", Toast.LENGTH_SHORT).show();

		for (Room current : this.roomService.getAllRoomConfigurations()) {

			EntityId mainSwitchId = new EntityId(EntityId.DOMAIN_SWITCH_VIRTUAL_MAIN, EntityId.ID_SWITCH_VIRTUAL_MAIN_SWITCH);

			if (current.switchesManager != null && current.switchesManager.switches != null
					&& current.switchesManager.switches.containsKey(mainSwitchId)) {
				try {
					RestClient.setSwitchablePowerState(current.roomName, mainSwitchId, false);
					Log.i(LOG, "switching off mainswitch for room: " + current.roomName);
				} catch (Exception e) {
					Toast.makeText(this, "Could not sitch off roomName: " + current.roomName, Toast.LENGTH_SHORT).show();
					Log.e(LOG, "Could not sitch off roomName: " + current.roomName, e);
				}
			}
		}
	}

}
