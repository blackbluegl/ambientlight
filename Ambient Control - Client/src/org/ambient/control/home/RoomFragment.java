/*  Copyright 2013 Florian Bornkessel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.ambient.control.home;

import org.ambient.control.R;
import org.ambient.control.RoomServiceAwareFragment;
import org.ambient.control.config.EditConfigOnExitListener;
import org.ambient.control.home.roomitems.ItemAdapter;
import org.ambient.rest.RestClient;
import org.ambient.util.RoomUtil;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.room.entities.features.Entity;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.ws.Room;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;


/**
 * @author Florian Bornkessel
 * 
 */
public class RoomFragment extends RoomServiceAwareFragment implements EditConfigOnExitListener {

	public static final String LOG = "RoomFragment";

	// for EditConfigExitListener - handles the preview save action for the led configuration dialog
	public static final String BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM = "actorConductConfigurationAfterEditItem";
	public RenderingProgramConfiguration actorConductConfigurationAfterEditItem;

	public static final String BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM_Id = "actorConductConfigurationAfterEditItemId";
	public EntityId actorConductConfigurationAfterEditItemId;

	private String actorConductConfigurationAfterEditItemRoomName;

	public static final String BUNDLE_ROOM_NAME = "roomNames";
	public String roomName;

	private View myRoomView;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.roomName = getArguments().getString(BUNDLE_ROOM_NAME);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// maybe restore the configuration for the last edited lightObject
		handleExitEditConductResult(savedInstanceState);

		// create the home container
		myRoomView = inflater.inflate(R.layout.fragment_home_room, null);

		return myRoomView;
	}


	/*
	 * fill values into the rooms after they have been created and tRoomhe service is available.
	 * 
	 * @see org.ambient.control.RoomServiceAwareFragment# initViewValuesAfterServiceConnection()
	 */
	@Override
	protected void onResumeWithServiceConnected() {
		updateRoomContent();
	}


	@Override
	public void onSaveInstanceState(Bundle bundle) {
		bundle.putSerializable(BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM, this.actorConductConfigurationAfterEditItem);
		bundle.putSerializable(BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM_Id, this.actorConductConfigurationAfterEditItemId);
		super.onSaveInstanceState(bundle);
	}


	/*
	 * get shure that the oldConfig will be overwritten. onCreateView will now save the new configuration
	 * 
	 * 
	 * @see org.ambient.control.config.EditConfigExitListener#onIntegrateConfiguration (java.lang.String, java.lang.Object)
	 */
	@Override
	public void onIntegrateConfiguration(String roomName, Object configuration) {
		this.actorConductConfigurationAfterEditItemRoomName = roomName;
		this.actorConductConfigurationAfterEditItem = (RenderingProgramConfiguration) configuration;
	}


	/*
	 * get sure that the oldConfig will not be overwritten. onCreateView will now save the old configuration
	 * 
	 * @see org.ambient.control.config.EditConfigExitListener#onRevertConfiguration (java.lang.String, java.lang.Object)
	 */
	@Override
	public void onRevertConfiguration(String roomName, Object configuration) {
		this.actorConductConfigurationAfterEditItemRoomName = roomName;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.RoomConfigAdapter.RoomUpdateListener #onRoomChange(java.lang.String, org.ambientlight.room.Room)
	 */

	@Override
	public void onRoomConfigurationChange(String serverName, Room config) {

		// prevent updating the ui if the fragment is not visible
		if (isVisible() == false)
			return;
		if (config != null) {
			updateRoomContent();
			enableEventListener();
		} else {
			disableEventListener(false);
		}
	}


	/**
	 * @param savedInstanceState
	 */
	private void handleExitEditConductResult(Bundle savedInstanceState) {

		// if this fragment was restored. restore the values from the bundle
		if (savedInstanceState != null) {
			actorConductConfigurationAfterEditItemId = (EntityId) savedInstanceState
					.getSerializable(BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM_Id);
			actorConductConfigurationAfterEditItem = (RenderingProgramConfiguration) savedInstanceState
					.getSerializable(BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM);
		}

		// write an edited item to server. weather it was modified to persist new state or not to revert to the old state due
		// inconsistent values on server because of preview actions
		if (actorConductConfigurationAfterEditItem != null) {
			RestClient.setRenderingConfiguration(this.actorConductConfigurationAfterEditItemRoomName,
					this.actorConductConfigurationAfterEditItemId, this.actorConductConfigurationAfterEditItem);
			actorConductConfigurationAfterEditItem = null;
		}
	}


	private void updateRoomContent() {

		Room roomConfig = roomService.getRoomConfiguration(roomName);
		if (roomConfig == null) {
			disableEventListener(false);
			return;
		}

		// init room power switch
		for (Entity currentSwitch : RoomUtil.getEntities(roomConfig)) {
			if (currentSwitch.getId().domain.equals(EntityId.DOMAIN_SWITCH_VIRTUAL_MAIN)) {
				createSwitch((Switchable) currentSwitch);
				break;
			}
		}

		// init scenery Spinner
		this.updateScenerySpinner();

		// init dynamically the clickable entity icons
		createRoomItems(roomConfig);

		// init roomBackground
		this.updateRoomBackground();
	}


	private void updateScenerySpinner() {
		final Room room = roomService.getRoomConfiguration(roomName);

		String[] sceneryNames = room.sceneriesManager.sceneries.keySet().toArray(new String[1]);

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item,
				sceneryNames);
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

		final Spinner spinner = (Spinner) myRoomView.findViewById(R.id.spinnerSceneries);
		spinner.setAdapter(adapter);
		adapter.getPosition(room.sceneriesManager.currentScenery.id);
		spinner.setSelection(adapter.getPosition(room.sceneriesManager.currentScenery.id));

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String selectedScenery = (String) parent.getItemAtPosition(pos);
				String currentScenery = room.sceneriesManager.currentScenery.id;

				if (!selectedScenery.equals(currentScenery)) {
					RestClient.setCurrentScenery(roomName, selectedScenery);
					spinner.setSelection(adapter.getPosition(selectedScenery));
					disableEventListener(true);
				}
			}


			@Override
			public void onNothingSelected(AdapterView<?> paramAdapterView) {

			}
		});
	}


	private void updateRoomBackground() {

		RelativeLayout roomBackground = (RelativeLayout) myRoomView.findViewById(R.id.roomBackground);
		Room roomConfig = roomService.getRoomConfiguration(roomName);

		if (RoomUtil.anySwitchTurnedOn(roomConfig)) {
			roomBackground.setBackgroundResource(R.drawable.bg_room_active);
		} else {
			roomBackground.setBackgroundResource(R.drawable.bg_room_disabled);
		}
	}


	/**
	 * @param currentServer
	 * @param roomConfig
	 * @param roomContent
	 * @param amountPerRow
	 */
	private void createRoomItems(Room roomConfig) {

		GridView contentView = (GridView) myRoomView.findViewById(R.id.roomContent);
		contentView.setAdapter(new ItemAdapter(RoomUtil.getEntities(roomConfig), roomConfig, this));
	}


	private void createSwitch(final Switchable currentSwitch) {

		final Switch mainSwitch = (Switch) myRoomView.findViewById(R.id.mainSwitch);

		mainSwitch.setChecked(currentSwitch.getPowerState());
		mainSwitch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					disableEventListener(true);

					RestClient.setSwitchablePowerState(roomName, currentSwitch.getId(), mainSwitch.isChecked());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	private void disableEventListener(boolean showWaitScreen) {
		if (showWaitScreen) {
			ProgressBar bar = (ProgressBar) myRoomView.findViewById(R.id.progressBar);
			bar.setVisibility(ProgressBar.VISIBLE);
		}
		GridView roomContent = (GridView) myRoomView.findViewById(R.id.roomContent);
		roomContent.setVisibility(GridView.INVISIBLE);

		LinearLayout bottomBar = (LinearLayout) myRoomView.findViewById(R.id.roomBottomBar);
		bottomBar.setVisibility(View.GONE);
	}


	private void enableEventListener() {
		ProgressBar bar = (ProgressBar) myRoomView.findViewById(R.id.progressBar);
		bar.setVisibility(ProgressBar.INVISIBLE);

		GridView roomContent = (GridView) myRoomView.findViewById(R.id.roomContent);
		roomContent.setVisibility(TableLayout.VISIBLE);

		LinearLayout bottomBar = (LinearLayout) myRoomView.findViewById(R.id.roomBottomBar);
		bottomBar.setVisibility(View.VISIBLE);
	}

}
