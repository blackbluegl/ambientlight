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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ambient.control.R;
import org.ambient.control.RoomServiceAwareFragment;
import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambient.control.config.EditConfigOnExitListener;
import org.ambient.control.home.mapper.AbstractRoomItemViewMapper;
import org.ambient.control.home.mapper.SimpleColorLightItemViewMapper;
import org.ambient.control.home.mapper.SunsetLightItemViewMapper;
import org.ambient.control.home.mapper.SwitchItemViewMapper;
import org.ambient.control.home.mapper.TronLightItemViewMapper;
import org.ambient.rest.RestClient;
import org.ambient.util.GuiUtils;
import org.ambient.views.ImageViewWithContextMenuInfo;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SunSetRenderingProgrammConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.TronRenderingProgrammConfiguration;
import org.ambientlight.room.entities.features.Entity;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.actor.types.SwitchType;
import org.ambientlight.room.entities.features.lightobject.Renderable;
import org.ambientlight.ws.Room;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


/**
 * @author Florian Bornkessel
 * 
 */
public class RoomFragment extends RoomServiceAwareFragment implements EditConfigOnExitListener {

	public static final String LOG = "RoomFragment";

	// size of the action icons in room
	private final int LIGHT_OBJECT_SIZE_DP = 85;

	// for EditConfigExitListener - handles the preview save action for the led configuration dialog
	public static final String BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM = "actorConductConfigurationAfterEditItem";
	private RenderingProgramConfiguration actorConductConfigurationAfterEditItem;

	public static final String BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM_NAME = "actorConductConfigurationAfterEditItemName";
	private String actorConductConfigurationAfterEditItemName;

	private String actorConductConfigurationAfterEditItemRoomName;

	public static final String BUNDLE_ROOM_NAMES = "roomNames";
	private List<String> roomNames;

	/*
	 * the lightobjectMappers will store information about the configuration of an lightobject like name, state and type. reinit
	 * is handled within the fragment
	 */
	private final Map<String, List<AbstractRoomItemViewMapper>> configuredlightObjects = new HashMap<String, List<AbstractRoomItemViewMapper>>();

	private LinearLayout roomsContainerView;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.roomNames = getArguments().getStringArrayList(BUNDLE_ROOM_NAMES);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// maybe restore the configuration for the last edited lightObject
		handleExitEditConductResult(savedInstanceState);

		// create the home container
		View myHomeScrollView = inflater.inflate(R.layout.fragment_rooms, null);
		roomsContainerView = (LinearLayout) myHomeScrollView.findViewById(R.id.linearLayoutRooms);
		roomsContainerView.setTag("roomContainers");

		LinearLayout roofTop = (LinearLayout) inflater.inflate(R.layout.fragment_home_rooftop, roomsContainerView, false);
		roomsContainerView.addView(roofTop);

		createMasterButton(roofTop);

		for (String currentRoom : roomNames) {

			LinearLayout roomContainerView = (LinearLayout) inflater.inflate(R.layout.layout_room, null);
			roomContainerView.setTag("roomContainer" + currentRoom);
			roomsContainerView.addView(roomContainerView);
			TableLayout roomContent = (TableLayout) roomContainerView.findViewById(R.id.roomContent);
			roomContent.setTag("roomContent" + currentRoom);

			Spinner spinner = (Spinner) roomContainerView.findViewById(R.id.spinnerSceneries);
			spinner.setTag("sceneries" + currentRoom);

			RelativeLayout roomBackground = (RelativeLayout) roomContainerView.findViewById(R.id.roomBackground);
			roomBackground.setTag("roomBackground" + currentRoom);

			ProgressBar bar = (ProgressBar) roomContainerView.findViewById(R.id.progressBar);
			bar.setTag("progressBar" + currentRoom);

			LinearLayout bottomBar = (LinearLayout) roomContainerView.findViewById(R.id.roomBottomBar);
			bottomBar.setTag("bottomBar" + currentRoom);
		}

		return myHomeScrollView;
	}


	/*
	 * fill values into the rooms after they have been created and tRoomhe service is available.
	 * 
	 * @see org.ambient.control.RoomServiceAwareFragment# initViewValuesAfterServiceConnection()
	 */
	@Override
	protected void onResumeWithServiceConnected() {
		for (String currentRoom : roomNames) {
			updateRoomContent(currentRoom);
		}

		this.updateRoofTop();
	}


	@Override
	public void onSaveInstanceState(Bundle bundle) {
		bundle.putSerializable(BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM, this.actorConductConfigurationAfterEditItem);
		bundle.putSerializable(BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM_NAME, this.actorConductConfigurationAfterEditItemName);
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

		// we get update callbacks but if the ui is not prepared and present we
		// would run into trouble
		if (isVisible() == false)
			return;
		if (config != null) {
			updateRoomContent(serverName);
			this.enableEventListener(serverName);
		} else {
			disableEventListener(serverName, false);
		}
		updateRoofTop();

	}


	/**
	 * @param savedInstanceState
	 */
	private void handleExitEditConductResult(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			actorConductConfigurationAfterEditItemName = savedInstanceState.getString(BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM_NAME);
			// onIntegrate was not called before and the fragment will be restored
			actorConductConfigurationAfterEditItem = (RenderingProgramConfiguration) savedInstanceState
					.getSerializable(BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM);
		}

		// write an edited item to server. weather it was modified to persist new state or not to revert to the old state due
		// inconsistent values on server because of preview actions
		if (actorConductConfigurationAfterEditItem != null) {
			RestClient.setRenderingConfiguration(this.actorConductConfigurationAfterEditItemRoomName,
					this.actorConductConfigurationAfterEditItemName, this.actorConductConfigurationAfterEditItem);
			actorConductConfigurationAfterEditItem = null;
		}
	}


	private void updateRoomContent(String currentRoom) {

		Room roomConfig = roomService.getRoomConfiguration(currentRoom);
		if (roomConfig == null) {
			disableEventListener(currentRoom, false);
			return;
		}

		if (this.configuredlightObjects != null && this.configuredlightObjects.get(currentRoom) != null) {
			this.configuredlightObjects.get(currentRoom).clear();
		}

		LinearLayout roomContainerView = (LinearLayout) roomsContainerView.findViewWithTag("roomContainer" + currentRoom);

		// init roomLabel
		TextView roomLabel = (TextView) roomContainerView.findViewById(R.id.textViewRoomName);
		roomLabel.setText(roomConfig.roomName);

		// init room power switch dynamically
		LinearLayout switchesView = (LinearLayout) roomContainerView.findViewById(R.id.linearLayoutRoomSwitches);
		switchesView.removeAllViews();
		for (org.ambientlight.room.entities.switches.Switch currentSwitch : roomConfig.switchesManager.switches.values()) {
			if (currentSwitch.getType().equals(SwitchType.VIRTUAL) || currentSwitch.getType().equals(SwitchType.VIRTUAL_MAIN)) {
				createSwitch(currentRoom, switchesView, currentSwitch);
			}
		}

		// init scenery Spinner
		this.updateScenerySpinner(currentRoom);

		// init dynamically the clickable light object icons
		TableLayout roomContent = (TableLayout) roomContainerView.findViewById(R.id.roomContent);
		roomContent.removeAllViews();
		createRoomItems(currentRoom, roomConfig, roomContent);

		// init roomBackground
		this.updateRoomBackground(currentRoom);
	}


	private void updateRoofTop() {
		// update rooftop
		boolean anyActive = false;

		for (Room currentConfig : roomService.getAllRoomConfigurations()) {
			if (currentConfig == null) {
				continue;
			}

			for (Switchable current : currentConfig.switchables) {
				if (current.getPowerState()) {
					anyActive = true;
					break;
				}
			}
		}

		this.setMasterSwitchState(anyActive);
	}


	private void updateScenerySpinner(final String currentRoom) {
		final Room room = roomService.getRoomConfiguration(currentRoom);

		String[] sceneryNames = room.sceneriesManager.sceneries.keySet().toArray(new String[1]);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item,
				sceneryNames);
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

		Spinner spinner = (Spinner) roomsContainerView.findViewWithTag("sceneries" + currentRoom);
		spinner.setAdapter(adapter);
		adapter.getPosition(room.sceneriesManager.currentScenery.id);
		spinner.setSelection(adapter.getPosition(room.sceneriesManager.currentScenery.id));

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String selectedScenery = (String) parent.getItemAtPosition(pos);
				String currentScenery = room.sceneriesManager.currentScenery.id;

				if (!selectedScenery.equals(currentScenery)) {
					RestClient.setCurrentScenery(currentRoom, currentScenery);
					disableEventListener(currentRoom, true);
				}
			}


			@Override
			public void onNothingSelected(AdapterView<?> paramAdapterView) {

			}
		});
	}


	private void updateRoomBackground(String currentRoom) {
		boolean powerSwitchIsChecked = false;
		RelativeLayout roomBackground = (RelativeLayout) roomsContainerView.findViewWithTag("roomBackground" + currentRoom);
		Room roomConfig = roomService.getRoomConfiguration(currentRoom);

		for (Switchable currentSwitch : roomConfig.switchables) {
			if (currentSwitch.getPowerState() == true) {
				powerSwitchIsChecked = true;
				break;
			}
		}

		for (AbstractRoomItemViewMapper current : this.configuredlightObjects.get(currentRoom)) {
			if (current.getPowerState() != powerSwitchIsChecked) {
				roomBackground.setBackgroundResource(R.drawable.bg_room_half_active);
				return;
			}
		}

		if (this.getPowerStateForAllLightObjectsInRoom(currentRoom)) {
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
	private void createRoomItems(String currentRoom, Room roomConfig, TableLayout roomContent) {
		int amountPerRow = getLightObjectAmountPerRow();
		TableRow row = new TableRow(roomContent.getContext());
		row.setGravity(Gravity.CENTER);
		roomContent.addView(row);

		List<Entity> entitiesForList = new ArrayList<Entity>();
		entitiesForList.addAll(roomConfig.lightObjectManager.lightObjects.values());
		entitiesForList.addAll(roomConfig.remoteSwitchesManager.remoteSwitches.values());

		Iterator<Entity> iterator = entitiesForList.iterator();
		for (int i = 0; i < roomConfig.lightObjectManager.lightObjects.size(); i++) {

			// create a new row if last one is full
			if (i % amountPerRow == 0) {
				row = new TableRow(roomContent.getContext());
				row.setGravity(Gravity.CENTER);
				roomContent.addView(row);
			}
			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View lightObject = inflater.inflate(R.layout.layout_room_item, null);
			createRoomItem(currentRoom, iterator.next(), lightObject);

			row.addView(lightObject);
		}
	}


	private void createRoomItem(final String currentRoom, final Entity currentConfig, View lightObjectView) {

		final AbstractRoomItemViewMapper roomItemMapper = getLightObjectMapperForLightObjectIcon(currentConfig, lightObjectView);

		List<AbstractRoomItemViewMapper> configuredlightObjects = this.configuredlightObjects.get(currentRoom);
		if (configuredlightObjects == null) {
			configuredlightObjects = new ArrayList<AbstractRoomItemViewMapper>();
			this.configuredlightObjects.put(currentRoom, configuredlightObjects);
		}

		configuredlightObjects.add(roomItemMapper);

		ImageViewWithContextMenuInfo icon = (ImageViewWithContextMenuInfo) lightObjectView
				.findViewById(R.id.imageViewLightObject);
		icon.setTag(roomItemMapper);
		registerForContextMenu(icon);
		icon.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// this long click shall only handle renderables
				if (currentConfig instanceof Renderable == false)
					return false;

				RoomFragment.this.actorConductConfigurationAfterEditItem = (RenderingProgramConfiguration) GuiUtils
						.deepCloneSerializeable(((Renderable) currentConfig).getRenderingProgrammConfiguration());
				RoomFragment.this.actorConductConfigurationAfterEditItemName = currentConfig.getId();

				getActivity().startActionMode(new ActionMode.Callback() {

					@Override
					public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

						switch (item.getItemId()) {

						case R.id.menuEntryEditActorConduct:
							ActorConductEditFragment fragEdit = new ActorConductEditFragment();
							fragEdit.setTargetFragment(RoomFragment.this, EditConfigHandlerFragment.REQ_RETURN_OBJECT);
							Bundle arguments = new Bundle();
							arguments.putSerializable(EditConfigHandlerFragment.BUNDLE_OBJECT_VALUE, (Entity) GuiUtils
									.deepCloneSerializeable(((Renderable) currentConfig).getRenderingProgrammConfiguration()));
							arguments.putBoolean(EditConfigHandlerFragment.ARG_CREATE_MODE, false);
							arguments.putString(EditConfigHandlerFragment.ARG_SELECTED_ROOM, currentRoom);
							arguments.putString(ActorConductEditFragment.ITEM_NAME, currentConfig.getId());

							fragEdit.setArguments(arguments);

							FragmentTransaction ft2 = getFragmentManager().beginTransaction();
							ft2.replace(R.id.LayoutMain, fragEdit);
							ft2.addToBackStack(null);
							ft2.commit();
							break;

						case R.id.menuEntryAddActorConduct:
							ActorConductEditFragment.createNewConfigBean(RenderingProgramConfiguration.class, RoomFragment.this,
									currentRoom, roomService.getRoomConfiguration(currentRoom), currentConfig.getId());
							break;

						default:
							break;

						}
						mode.finish();
						return true;
					}


					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {
						mode.getMenuInflater().inflate(R.menu.fragment_room_cab, menu);
						return true;
					}


					@Override
					public void onDestroyActionMode(ActionMode mode) {
					}


					@Override
					public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
						return false;
					}
				});
				return true;
			}
		});

		// a click on an icon toggles the powerstate on the server
		icon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// while updateing the roomContent the Icons will be disabled
				if (roomItemMapper.isEventListenerDisabled())
					return;

				try {

					// only switch switchables
					if (currentConfig instanceof Switchable == false)
						return;

					Switchable switchable = (Switchable) currentConfig;
					RestClient.setSwitchablePowerState(currentRoom, switchable.getType(), switchable.getId(),
							!roomItemMapper.getPowerState());

					// update the icon - we prevent that the user experiences a
					// gap (request to server, response to callback)
					roomItemMapper.setPowerState(!roomItemMapper.getPowerState());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	private void createSwitch(final String currentRoom, LinearLayout switchesView,
			final org.ambientlight.room.entities.switches.Switch currentEventGenerator) {

		final Switch powerStateSwitch = new Switch(this.getActivity());
		powerStateSwitch.setTag("powerStateSwitch" + currentRoom + currentEventGenerator.getId());
		switchesView.addView(powerStateSwitch, 0);
		powerStateSwitch.setChecked(currentEventGenerator.getPowerState());
		powerStateSwitch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					disableEventListener(currentRoom, true);

					RestClient.setSwitchablePowerState(currentRoom, currentEventGenerator.getType(),
							currentEventGenerator.getId(), powerStateSwitch.isChecked());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	/**
	 * @param roofTop
	 */
	private void createMasterButton(LinearLayout roofTop) {
		ImageView masterButton = (ImageView) roofTop.findViewById(R.id.imageViewMasterSwitch);

		masterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				for (String currentRoomName : roomNames) {
					try {
						org.ambientlight.ws.Room currentConfig = roomService.getRoomConfiguration(currentRoomName);

						for (Switchable currentEventGenerator : currentConfig.switchables) {
							RestClient.setSwitchablePowerState(currentRoomName, currentEventGenerator.getType(),
									currentEventGenerator.getId(), false);
						}
					} catch (Exception e) {
						Log.e(LOG, "error switching masterswitch", e);
					}
				}
				setMasterSwitchState(false);
			}
		});
	}


	private void setMasterSwitchState(boolean powerState) {

		ImageView masterSwitch = (ImageView) this.roomsContainerView.findViewById(R.id.imageViewMasterSwitch);

		if (powerState == true) {
			masterSwitch.setImageResource(R.drawable.ic_power_active);
		} else {
			masterSwitch.setImageResource(R.drawable.ic_power_disabled);
		}
	}


	private void disableEventListener(String currentRoom, boolean showWaitScreen) {
		if (showWaitScreen) {
			ProgressBar bar = (ProgressBar) roomsContainerView.findViewWithTag("progressBar" + currentRoom);
			bar.setVisibility(ProgressBar.VISIBLE);
		}
		TableLayout roomContent = (TableLayout) roomsContainerView.findViewWithTag("roomContent" + currentRoom);
		roomContent.setVisibility(TableLayout.INVISIBLE);

		LinearLayout bottomBar = (LinearLayout) roomsContainerView.findViewWithTag("bottomBar" + currentRoom);
		bottomBar.setVisibility(View.GONE);

		if (this.configuredlightObjects != null && this.configuredlightObjects.get(currentRoom) != null) {
			for (AbstractRoomItemViewMapper mapper : configuredlightObjects.get(currentRoom)) {
				mapper.setEventListenerDisabled(true);
			}
		}

	}


	private void enableEventListener(String serverName) {
		ProgressBar bar = (ProgressBar) roomsContainerView.findViewWithTag("progressBar" + serverName);
		bar.setVisibility(ProgressBar.INVISIBLE);

		TableLayout roomContent = (TableLayout) roomsContainerView.findViewWithTag("roomContent" + serverName);
		roomContent.setVisibility(TableLayout.VISIBLE);

		LinearLayout bottomBar = (LinearLayout) roomsContainerView.findViewWithTag("bottomBar" + serverName);
		bottomBar.setVisibility(View.VISIBLE);

		for (AbstractRoomItemViewMapper mapper : configuredlightObjects.get(serverName)) {
			mapper.setEventListenerDisabled(false);
		}
	}


	private int getLightObjectAmountPerRow() {
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		float dp = getResources().getDisplayMetrics().widthPixels;

		final float scale = getResources().getDisplayMetrics().density;
		int containerSize = 0;
		if (GuiUtils.isLargeLayout(getActivity()) == false
				|| getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			containerSize = ((int) (dp / scale + 0.5f));
		} else {
			containerSize = (int) ((int) (dp / scale + 0.5f) * 0.66f);
		}

		int amountPerRow = containerSize / this.LIGHT_OBJECT_SIZE_DP;
		return amountPerRow;
	}


	private AbstractRoomItemViewMapper getLightObjectMapperForLightObjectIcon(final Entity currentConfig, View lightObjectView) {
		AbstractRoomItemViewMapper result = null;

		if (currentConfig instanceof Renderable && currentConfig instanceof Switchable) {

			RenderingProgramConfiguration sceneryConfig = ((Renderable) currentConfig).getRenderingProgrammConfiguration();
			boolean powerState = ((Switchable) currentConfig).getPowerState();

			if (sceneryConfig instanceof SimpleColorRenderingProgramConfiguration) {
				result = new SimpleColorLightItemViewMapper(lightObjectView, currentConfig.getId(),
						R.string.program_simple_color, powerState);
			}

			if (sceneryConfig instanceof TronRenderingProgrammConfiguration) {
				result = new TronLightItemViewMapper(lightObjectView, currentConfig.getId(), R.string.program_tron, powerState);
			}

			if (sceneryConfig instanceof SunSetRenderingProgrammConfiguration) {
				result = new SunsetLightItemViewMapper(lightObjectView, currentConfig.getId(), R.string.program_tron, powerState);
			}
		} else if (currentConfig instanceof Switchable) {
			boolean powerState = ((Switchable) currentConfig).getPowerState();
			result = new SwitchItemViewMapper(lightObjectView, currentConfig.getId(), powerState);
		}
		return result;
	}


	private boolean getPowerStateForAllLightObjectsInRoom(String currentRoom) {
		for (AbstractRoomItemViewMapper current : this.configuredlightObjects.get(currentRoom)) {
			if (current.getPowerState() == true)
				return true;
		}
		return false;
	}
}
