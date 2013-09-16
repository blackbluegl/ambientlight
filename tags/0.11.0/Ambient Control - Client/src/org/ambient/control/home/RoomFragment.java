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

import org.ambient.control.MainActivity;
import org.ambient.control.R;
import org.ambient.control.RoomConfigManager.RoomConfigurationUpdateListener;
import org.ambient.control.config.EditConfigExitListener;
import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambient.control.home.mapper.AbstractRoomItemViewMapper;
import org.ambient.control.home.mapper.SimpleColorLightItemViewMapper;
import org.ambient.control.home.mapper.SwitchItemViewMapper;
import org.ambient.control.home.mapper.TronLightItemViewMapper;
import org.ambient.control.rest.RestClient;
import org.ambient.util.GuiUtils;
import org.ambient.views.ImageViewWithContextMenuInfo;
import org.ambient.widgets.WidgetUtils;
import org.ambientlight.process.events.SceneryEntryEventConfiguration;
import org.ambientlight.process.events.SwitchEventConfiguration;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.actors.ActorConfiguration;
import org.ambientlight.room.eventgenerator.SceneryEventGeneratorConfiguration;
import org.ambientlight.room.eventgenerator.SwitchEventGeneratorConfiguration;
import org.ambientlight.scenery.AbstractSceneryConfiguration;
import org.ambientlight.scenery.actor.ActorConductConfiguration;
import org.ambientlight.scenery.actor.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.scenery.actor.renderingprogram.TronRenderingProgrammConfiguration;
import org.ambientlight.scenery.actor.switching.SwitchingConfiguration;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
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
public class RoomFragment extends Fragment implements RoomConfigurationUpdateListener, EditConfigExitListener {

	public static final String BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM = "actorConductConfigurationAfterEditItem";
	protected ActorConductConfiguration actorConductConfigurationAfterEditItem;
	public static final String BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM_NAME = "actorConductConfigurationAfterEditItemName";
	protected String actorConductConfigurationAfterEditItemName;
	protected String actorConductConfigurationAfterEditItemServerName;

	private final int LIGHT_OBJECT_SIZE_DP = 85;

	/*
	 * the lightobjectMappers will store information about the configuration of
	 * an lightobject like name, state and type. reinit is handled within the
	 * fragment
	 */
	private final Map<String, List<AbstractRoomItemViewMapper>> configuredlightObjects = new HashMap<String, List<AbstractRoomItemViewMapper>>();


	public static final String BUNDLE_SERVER_NAMES = "serverName";
	private List<String> serverNames;
	LinearLayout roomsContainerView;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (savedInstanceState != null) {
			actorConductConfigurationAfterEditItemName = savedInstanceState.getString(BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM_NAME);
			// onIntegrate was not called before and the fragment will be
			// restored
			if (actorConductConfigurationAfterEditItem == null) {
				actorConductConfigurationAfterEditItem = (ActorConductConfiguration) savedInstanceState
						.getSerializable(BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM);
			}
		}

		// write an edited item to server. wether it was modified to persist new
		// state or not to revert to the old state due inconsistent values on
		// server because of preview actions
		if (actorConductConfigurationAfterEditItem != null) {
			RestClient rest = new RestClient(((MainActivity) getActivity()).getRoomConfigManager());
			rest.setActorConductConfiguration(this.actorConductConfigurationAfterEditItemServerName,
					this.actorConductConfigurationAfterEditItemName, this.actorConductConfigurationAfterEditItem);
			actorConductConfigurationAfterEditItem = null;
		}

		this.serverNames = getArguments().getStringArrayList(BUNDLE_SERVER_NAMES);

		// create the home container
		View myHomeScrollView = inflater.inflate(R.layout.fragment_rooms, null);
		roomsContainerView = (LinearLayout) myHomeScrollView.findViewById(R.id.linearLayoutRooms);
		roomsContainerView.setTag("roomContainers");

		LinearLayout roofTop = (LinearLayout) inflater.inflate(R.layout.fragment_home_rooftop, roomsContainerView, false);
		roomsContainerView.addView(roofTop);

		ImageView masterButton = (ImageView) roofTop.findViewById(R.id.imageViewMasterSwitch);

		masterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				for (String currentServerName : serverNames) {
					try {
						RoomConfiguration currentConfig = ((MainActivity) getActivity()).getRoomConfigManager()
								.getRoomConfiguration(currentServerName);

						for (SwitchEventGeneratorConfiguration currentEventGenerator : currentConfig.getSwitchGenerators()
								.values()) {
							SwitchEventConfiguration event = new SwitchEventConfiguration();
							event.eventGeneratorName = currentEventGenerator.getName();
							event.powerState = false;
							getRestClient().sendEvent(currentServerName, event);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				updateMasterSwitchState(false);
			}
		});

		for (String currentServer : serverNames) {
			RoomConfiguration roomConfig = ((MainActivity) getActivity()).getRoomConfigManager().getRoomConfiguration(
					currentServer);
			LinearLayout roomContainerView = (LinearLayout) inflater.inflate(R.layout.layout_room, null);
			roomsContainerView.addView(roomContainerView);
			TableLayout roomContent = (TableLayout) roomContainerView.findViewById(R.id.roomContent);
			roomContent.setTag("roomContent" + currentServer);
			TextView roomLabel = (TextView) roomContainerView.findViewById(R.id.textViewRoomName);
			roomLabel.setText(roomConfig.roomName);
			roomLabel.setTag("roomLabel");
			// init dynamically the clickable light object icons
			int amountPerRow = getLightObjectAmountPerRow();

			TableRow row = new TableRow(roomContent.getContext());
			row.setGravity(Gravity.CENTER);
			roomContent.addView(row);
			Iterator<ActorConfiguration> iterator = roomConfig.actorConfigurations.values().iterator();
			for (int i = 0; i < roomConfig.actorConfigurations.size(); i++) {

				// create a new row if last one is full
				if (i % amountPerRow == 0) {
					row = new TableRow(roomContent.getContext());
					row.setGravity(Gravity.CENTER);
					roomContent.addView(row);
				}

				View lightObject = inflater.inflate(R.layout.layout_room_item, null);
				initRoomItemIconView(currentServer, iterator.next(), lightObject);

				row.addView(lightObject);
			}
			// init room power switch dynamically
			LinearLayout roomBottomBarView = (LinearLayout) roomContainerView.findViewById(R.id.roomBottomBar);

			for (SwitchEventGeneratorConfiguration currentEventGenerator : roomConfig.getSwitchGenerators().values()) {
				createSwitch(currentServer, roomBottomBarView, currentEventGenerator);
			}
			// init scenery Spinner
			Spinner spinner = (Spinner) roomContainerView.findViewById(R.id.spinnerSceneries);
			spinner.setTag("sceneries" + currentServer);
			this.updateScenerySpinner(currentServer);

			RelativeLayout roomBackground = (RelativeLayout) roomContainerView.findViewById(R.id.roomBackground);
			roomBackground.setTag("roomBackground" + currentServer);

			ProgressBar bar = (ProgressBar) roomContainerView.findViewById(R.id.progressBar);
			bar.setTag("progressBar" + currentServer);
		}

		updateRoofTop();

		return myHomeScrollView;
	}


	public void updateMasterSwitchState(boolean powerState) {

		ImageView masterSwitch = (ImageView) this.roomsContainerView.findViewById(R.id.imageViewMasterSwitch);

		if (powerState == true) {
			masterSwitch.setImageResource(R.drawable.ic_power_active);
		} else {
			masterSwitch.setImageResource(R.drawable.ic_power_disabled);
		}
	}


	/**
	 * @param sceneries
	 * @return
	 */
	private String[] getSceneryArray(List<AbstractSceneryConfiguration> sceneries) {
		String[] result = new String[sceneries.size()];
		for (int i = 0; i < sceneries.size(); i++) {
			result[i] = sceneries.get(i).id;
		}
		return result;
	}


	private void disableEventListener(String serverName) {
		ProgressBar bar = (ProgressBar) roomsContainerView.findViewWithTag("progressBar" + serverName);
		bar.setVisibility(ProgressBar.VISIBLE);
		TableLayout roomContent = (TableLayout) roomsContainerView.findViewWithTag("roomContent" + serverName);
		roomContent.setVisibility(TableLayout.INVISIBLE);

		// LinearLayout roomBottomBar = (LinearLayout)
		// roomContainerView.findViewById(R.id.roomBottomBar);
		// roomBottomBar.setVisibility(LinearLayout.INVISIBLE);

		for (AbstractRoomItemViewMapper mapper : configuredlightObjects.get(serverName)) {
			mapper.setEventListenerDisabled(true);
		}
	}


	private void enableEventListener(String serverName) {
		ProgressBar bar = (ProgressBar) roomsContainerView.findViewWithTag("progressBar" + serverName);
		bar.setVisibility(ProgressBar.INVISIBLE);

		TableLayout roomContent = (TableLayout) roomsContainerView.findViewWithTag("roomContent" + serverName);
		roomContent.setVisibility(TableLayout.VISIBLE);

		// LinearLayout roomBottomBar = (LinearLayout)
		// roomContainerView.findViewById(R.id.roomBottomBar);
		// roomBottomBar.setVisibility(LinearLayout.VISIBLE);

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


	private void initRoomItemIconView(final String serverName, final ActorConfiguration currentConfig, View lightObjectView) {

		final AbstractRoomItemViewMapper roomItemMapper = getLightObjectMapperForLightObjectIcon(currentConfig, lightObjectView);

		List<AbstractRoomItemViewMapper> configuredlightObjects = this.configuredlightObjects.get(serverName);
		if (configuredlightObjects == null) {
			configuredlightObjects = new ArrayList<AbstractRoomItemViewMapper>();
			this.configuredlightObjects.put(serverName, configuredlightObjects);
		}

		configuredlightObjects.add(roomItemMapper);

		ImageViewWithContextMenuInfo icon = (ImageViewWithContextMenuInfo) lightObjectView
				.findViewById(R.id.imageViewLightObject);
		icon.setTag(roomItemMapper);
		registerForContextMenu(icon);
		final RoomFragment myself = this;
		icon.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				myself.actorConductConfigurationAfterEditItem = (ActorConductConfiguration) GuiUtils
						.deepCloneSerializeable(currentConfig.actorConductConfiguration);
				myself.actorConductConfigurationAfterEditItemName = currentConfig.getName();

				getActivity().startActionMode(new ActionMode.Callback() {

					// EditConfigExitListener exitCallback = new
					// EditConfigExitListener() {
					//
					// @Override
					// public void onIntegrateConfiguration(String serverName,
					// Object configuration) {
					//
					// RestClient rest = new RestClient(((MainActivity)
					// getActivity()).getRoomConfigManager());
					// rest.setActorConductConfiguration(serverName,
					// currentConfig.getName(),
					// (ActorConductConfiguration) configuration);
					//
					// }
					//
					//
					// @Override
					// public void onRevertConfiguration(String serverName,
					// Object configuration) {
					// RestClient rest = new RestClient(((MainActivity)
					// getActivity()).getRoomConfigManager());
					// rest.setActorConductConfiguration(serverName,
					// currentConfig.getName(), oldActorConductConfiguration);
					// }
					// };

					@Override
					public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

						switch (item.getItemId()) {

						case R.id.menuEntryEditActorConduct:
							ActorConductEditFragment fragEdit = new ActorConductEditFragment();
							fragEdit.setTargetFragment(myself, EditConfigHandlerFragment.REQ_RETURN_OBJECT);
							Bundle arguments = new Bundle();
							arguments.putSerializable(EditConfigHandlerFragment.OBJECT_VALUE,
									(ActorConductConfiguration) GuiUtils
									.deepCloneSerializeable(currentConfig.actorConductConfiguration));
							arguments.putBoolean(EditConfigHandlerFragment.CREATE_MODE, false);
							arguments.putString(EditConfigHandlerFragment.SELECTED_SERVER, serverName);
							arguments.putString(ActorConductEditFragment.ITEM_NAME, currentConfig.getName());

							fragEdit.setArguments(arguments);

							FragmentTransaction ft2 = getFragmentManager().beginTransaction();
							ft2.replace(R.id.LayoutMain, fragEdit);
							ft2.addToBackStack(null);
							ft2.commit();
							break;

						case R.id.menuEntryAddActorConduct:
							ActorConductEditFragment.createNewConfigBean(ActorConductConfiguration.class, myself, serverName,
									currentConfig.getName());
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
						// TODO Auto-generated method stub

					}


					@Override
					public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
						// TODO Auto-generated method stub
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
					getRestClient().setPowerStateForRoomItem(serverName, roomItemMapper.getItemName(),
							!roomItemMapper.getPowerState());
					// updateing the icon
					roomItemMapper.setPowerState(!roomItemMapper.getPowerState());

					updateRoomBackground(serverName);

					// update widgets
					WidgetUtils.notifyWidgets(getActivity());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	/**
	 * @param serverName
	 * @param callback
	 * @param roomBottomBarView
	 * @param currentEventGenerator
	 */
	public void createSwitch(final String serverName, LinearLayout roomBottomBarView,
			final SwitchEventGeneratorConfiguration currentEventGenerator) {

		final Switch powerStateSwitch = new Switch(this.getActivity());
		powerStateSwitch.setTag("powerStateSwitch" + serverName + currentEventGenerator.getName());
		roomBottomBarView.addView(powerStateSwitch, 0);
		powerStateSwitch.setChecked(currentEventGenerator.getPowerState());
		powerStateSwitch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					disableEventListener(serverName);
					SwitchEventConfiguration event = new SwitchEventConfiguration();
					event.eventGeneratorName = currentEventGenerator.getName();
					event.powerState = powerStateSwitch.isChecked();
					getRestClient().sendEvent(serverName, event);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	private AbstractRoomItemViewMapper getLightObjectMapperForLightObjectIcon(final ActorConfiguration currentConfig,
			View lightObjectView) {
		AbstractRoomItemViewMapper result = null;

		ActorConductConfiguration sceneryConfig = currentConfig.actorConductConfiguration;

		if (sceneryConfig instanceof SimpleColorRenderingProgramConfiguration) {
			result = new SimpleColorLightItemViewMapper(lightObjectView, currentConfig.getName(), R.string.program_simple_color,
					currentConfig.getPowerState());
		}

		if (sceneryConfig instanceof TronRenderingProgrammConfiguration) {
			result = new TronLightItemViewMapper(lightObjectView, currentConfig.getName(), R.string.program_tron,
					currentConfig.getPowerState());
		}

		if (sceneryConfig instanceof SwitchingConfiguration) {
			result = new SwitchItemViewMapper(lightObjectView, currentConfig.getName(), currentConfig.getPowerState());
		}

		return result;
	}


	public void updateScenerySpinner(final String serverName) {
		List<AbstractSceneryConfiguration> sceneries = ((MainActivity) getActivity()).getRoomConfigManager()
				.getRoomConfiguration(serverName).getSceneries();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item,
				getSceneryArray(sceneries));
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

		Spinner spinner = (Spinner) roomsContainerView.findViewWithTag("sceneries" + serverName);
		spinner.setAdapter(adapter);
		adapter.getPosition(getCurrentScenery(serverName).id);
		spinner.setSelection(adapter.getPosition(getCurrentScenery(serverName).id));

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String selectedScenery = (String) parent.getItemAtPosition(pos);
				String currentScenery = getCurrentScenery(serverName).id;

				if (!selectedScenery.equals(currentScenery)) {
					SceneryEntryEventConfiguration event = new SceneryEntryEventConfiguration();
					event.eventGeneratorName = "RoomSceneryEventGenerator";
					event.sceneryName = selectedScenery;
					getRestClient().sendEvent(serverName, event);
					disableEventListener(serverName);
				}
			}


			@Override
			public void onNothingSelected(AdapterView<?> paramAdapterView) {

			}
		});
	}


	private void updateRoomBackground(String serverName) {
		boolean powerSwitchIsChecked = false;
		RelativeLayout roomBackground = (RelativeLayout) roomsContainerView.findViewWithTag("roomBackground" + serverName);
		RoomConfiguration roomConfig = ((MainActivity) getActivity()).getRoomConfigManager().getRoomConfiguration(serverName);

		for (SwitchEventGeneratorConfiguration currentSwitch : roomConfig.getSwitchGenerators().values()) {
			if (currentSwitch.getPowerState() == true) {
				powerSwitchIsChecked = true;
				break;
			}
		}

		for (AbstractRoomItemViewMapper current : this.configuredlightObjects.get(serverName)) {
			if (current.getPowerState() != powerSwitchIsChecked) {
				roomBackground.setBackgroundResource(R.drawable.bg_room_half_active);
				return;
			}
		}

		if (this.getPowerStateForAllLightObjectsInRoom(serverName)) {
			roomBackground.setBackgroundResource(R.drawable.bg_room_active);
		} else {
			roomBackground.setBackgroundResource(R.drawable.bg_room_disabled);
		}
	}


	private boolean getPowerStateForAllLightObjectsInRoom(String serverName) {
		for (AbstractRoomItemViewMapper current : this.configuredlightObjects.get(serverName)) {
			if (current.getPowerState() == true)
				return true;
		}
		return false;
	}


	private AbstractSceneryConfiguration getCurrentScenery(String serverName) {
		RoomConfiguration roomConfig = ((MainActivity) getActivity()).getRoomConfigManager().getRoomConfiguration(serverName);
		for (SceneryEventGeneratorConfiguration eventGen : roomConfig.getSceneryEventGenerator().values())
			// we assume that there is only one per room
			return eventGen.currentScenery;
		return null;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambient.control.RoomConfigAdapter.RoomConfigurationUpdateListener
	 * #onRoomConfigurationChange(java.lang.String,
	 * org.ambientlight.room.RoomConfiguration)
	 */
	@Override
	public void onRoomConfigurationChange(String serverName, RoomConfiguration config) {

		List<AbstractRoomItemViewMapper> mappersToRefresh = new ArrayList<AbstractRoomItemViewMapper>(
				this.configuredlightObjects.get(serverName));

		this.configuredlightObjects.get(serverName).clear();

		for (AbstractRoomItemViewMapper currentToRefresh : mappersToRefresh) {
			ActorConfiguration currentConfig = config.actorConfigurations.get(currentToRefresh.getItemName());
			this.initRoomItemIconView(serverName, currentConfig, currentToRefresh.getLightObjectView());
		}

		// update switches
		for (SwitchEventGeneratorConfiguration currentEventGenerator : config.getSwitchGenerators().values()) {
			Switch currentSwitch = (Switch) this.roomsContainerView.findViewWithTag("powerStateSwitch" + serverName
					+ currentEventGenerator.name);
			currentSwitch.setChecked(currentEventGenerator.getPowerState());
		}

		updateRoomBackground(serverName);

		this.enableEventListener(serverName);

		// update widgets
		WidgetUtils.notifyWidgets(this.getActivity());

		updateRoofTop();

	}


	/**
	 * 
	 */
	public void updateRoofTop() {
		// update rooftop
		boolean anyActive = false;

		for (RoomConfiguration currentConfig : ((MainActivity) getActivity()).getRoomConfigManager().getAllRoomConfigurations()
				.values()) {

			for (SwitchEventGeneratorConfiguration currentEventGenerator : currentConfig.getSwitchGenerators().values()) {
				if (currentEventGenerator.getPowerState()) {
					anyActive = true;
					break;
				}
			}
		}

		this.updateMasterSwitchState(anyActive);
	}


	private RestClient getRestClient() {
		return ((MainActivity) this.getActivity()).getRestClient();
	}


	@Override
	public void onResume() {
		super.onResume();
	}


	/*
	 * get shure that the oldConfig will be overwritten. onCreateView will now
	 * save the new configuration
	 * 
	 * 
	 * @see
	 * org.ambient.control.config.EditConfigExitListener#onIntegrateConfiguration
	 * (java.lang.String, java.lang.Object)
	 */
	@Override
	public void onIntegrateConfiguration(String serverName, Object configuration) {
		this.actorConductConfigurationAfterEditItemServerName = serverName;
		this.actorConductConfigurationAfterEditItem = (ActorConductConfiguration) configuration;
	}


	/*
	 * get shure that the oldConfig will not be overwritten. onCreateView will
	 * now save the old configuration
	 * 
	 * @see
	 * org.ambient.control.config.EditConfigExitListener#onRevertConfiguration
	 * (java.lang.String, java.lang.Object)
	 */
	@Override
	public void onRevertConfiguration(String serverName, Object configuration) {
		this.actorConductConfigurationAfterEditItemServerName = serverName;
	}


	@Override
	public void onSaveInstanceState(Bundle bundle) {
		bundle.putSerializable(BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM, this.actorConductConfigurationAfterEditItem);
		bundle.putSerializable(BUNDLE_ACTOR_CONDUCT_AFTER_EDIT_ITEM_NAME, this.actorConductConfigurationAfterEditItemName);
		super.onSaveInstanceState(bundle);
	}
}
