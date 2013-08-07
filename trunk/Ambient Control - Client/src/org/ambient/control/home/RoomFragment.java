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
import java.util.Iterator;
import java.util.List;

import org.ambient.control.MainActivity;
import org.ambient.control.R;
import org.ambient.control.RoomConfigManager.RoomConfigurationUpdateListener;
import org.ambient.control.RoomConfigurationParceable;
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
import org.ambientlight.room.eventgenerator.EventGeneratorConfiguration;
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
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
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
public class RoomFragment extends Fragment implements RoomConfigurationUpdateListener {

	private final int LIGHT_OBJECT_SIZE_DP = 85;

	/*
	 * the lightobjectMappers will store information about the configuration of
	 * an lightobject like name, state and type. reinit is handled within the
	 * fragment
	 */
	private final List<AbstractRoomItemViewMapper> configuredlightObjects = new ArrayList<AbstractRoomItemViewMapper>();

	public static final String BUNDLE_ROOM_CONFIG = "roomConfig";
	private RoomConfiguration roomConfig;

	public static final String BUNDLE_SERVER_NAME = "serverName";
	private String serverName;

	private View roomContainerView;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		serverName = getArguments().getString(BUNDLE_SERVER_NAME);
		RoomConfigurationParceable roomConfigParceable = getArguments().getParcelable(BUNDLE_ROOM_CONFIG);
		this.roomConfig = roomConfigParceable.roomConfiguration;

		// create the room container
		roomContainerView = inflater.inflate(R.layout.fragment_room, null);
		roomContainerView.setTag("roomContainer");

		TableLayout roomContent = (TableLayout) roomContainerView.findViewById(R.id.roomContent);
		roomContent.setTag("roomContent");

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
			initRoomItemIconView(this.getCurrentScenery(), iterator.next(), lightObject);

			row.addView(lightObject);
		}

		// init room power switch dynamically
		LinearLayout roomBottomBarView = (LinearLayout) roomContainerView.findViewById(R.id.roomBottomBar);

		for (EventGeneratorConfiguration currentEventGenerator : roomConfig.eventGeneratorConfigurations) {
			if (currentEventGenerator instanceof SwitchEventGeneratorConfiguration) {
				createSwitch(serverName, roomBottomBarView, (SwitchEventGeneratorConfiguration) currentEventGenerator);
			}
		}

		// init scenery Spinner
		this.updateScenerySpinner();

		return roomContainerView;
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


	private void disableEventListener() {
		ProgressBar bar = (ProgressBar) roomContainerView.findViewById(R.id.progressBar);
		bar.setVisibility(ProgressBar.VISIBLE);
		TableLayout roomContent = (TableLayout) roomContainerView.findViewWithTag("roomContent");
		roomContent.setVisibility(TableLayout.INVISIBLE);

		// LinearLayout roomBottomBar = (LinearLayout)
		// roomContainerView.findViewById(R.id.roomBottomBar);
		// roomBottomBar.setVisibility(LinearLayout.INVISIBLE);

		for (AbstractRoomItemViewMapper mapper : configuredlightObjects) {
			mapper.setEventListenerDisabled(true);
		}
	}


	private void enableEventListener() {
		ProgressBar bar = (ProgressBar) roomContainerView.findViewById(R.id.progressBar);
		bar.setVisibility(ProgressBar.INVISIBLE);

		TableLayout roomContent = (TableLayout) roomContainerView.findViewWithTag("roomContent");
		roomContent.setVisibility(TableLayout.VISIBLE);

		// LinearLayout roomBottomBar = (LinearLayout)
		// roomContainerView.findViewById(R.id.roomBottomBar);
		// roomBottomBar.setVisibility(LinearLayout.VISIBLE);

		for (AbstractRoomItemViewMapper mapper : configuredlightObjects) {
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


	private void initRoomItemIconView(AbstractSceneryConfiguration sceneryName, final ActorConfiguration currentConfig,
			View lightObjectView) {

		final AbstractRoomItemViewMapper roomItemMapper = getLightObjectMapperForLightObjectIcon(sceneryName, currentConfig,
				lightObjectView);

		this.configuredlightObjects.add(roomItemMapper);

		ImageViewWithContextMenuInfo icon = (ImageViewWithContextMenuInfo) lightObjectView
				.findViewById(R.id.imageViewLightObject);
		icon.setTag(roomItemMapper);
		registerForContextMenu(icon);

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

					updateRoomBackground();

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
		powerStateSwitch.setTag("powerStateSwitch" + currentEventGenerator.getName());
		roomBottomBarView.addView(powerStateSwitch, 0);
		powerStateSwitch.setChecked(currentEventGenerator.getPowerState());
		powerStateSwitch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					disableEventListener();
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


	private AbstractRoomItemViewMapper getLightObjectMapperForLightObjectIcon(AbstractSceneryConfiguration sceneryName,
			final ActorConfiguration currentConfig, View lightObjectView) {
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


	public void updateScenerySpinner() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item,
				getSceneryArray(roomConfig.sceneries));
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

		Spinner spinner = (Spinner) roomContainerView.findViewById(R.id.spinnerSceneries);
		spinner.setAdapter(adapter);
		adapter.getPosition(getCurrentScenery().id);
		spinner.setSelection(adapter.getPosition(getCurrentScenery().id));

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String selectedScenery = (String) parent.getItemAtPosition(pos);
				String currentScenery = getCurrentScenery().id;

				if (!selectedScenery.equals(currentScenery)) {
					SceneryEntryEventConfiguration event = new SceneryEntryEventConfiguration();
					event.eventGeneratorName = "RoomSceneryEventGenerator";
					event.sceneryName = selectedScenery;
					getRestClient().sendEvent(serverName, event);
					disableEventListener();
				}
			}


			@Override
			public void onNothingSelected(AdapterView<?> paramAdapterView) {

			}
		});
	}


	private void updateRoomBackground() {
		boolean powerSwitchIsChecked = false;
		RelativeLayout roomBackground = (RelativeLayout) roomContainerView.findViewById(R.id.roomBackground);

		for (SwitchEventGeneratorConfiguration currentSwitch : this.roomConfig.getSwitchGenerators().values()) {
			if (currentSwitch.getPowerState() == true) {
				powerSwitchIsChecked = true;
				break;
			}
		}

		for (AbstractRoomItemViewMapper current : this.configuredlightObjects) {
			if (current.getPowerState() != powerSwitchIsChecked) {
				roomBackground.setBackgroundResource(R.drawable.bg_room_half_active);
				return;
			}
		}

		if (this.getPowerStateForAllLightObjectsInRoom()) {
			roomBackground.setBackgroundResource(R.drawable.bg_room_active);
		} else {
			roomBackground.setBackgroundResource(R.drawable.bg_room_disabled);
		}
	}


	private boolean getPowerStateForAllLightObjectsInRoom() {
		for (AbstractRoomItemViewMapper current : this.configuredlightObjects) {
			if (current.getPowerState() == true)
				return true;
		}
		return false;
	}


	private AbstractSceneryConfiguration getCurrentScenery() {
		for (EventGeneratorConfiguration eventGen : this.roomConfig.eventGeneratorConfigurations) {
			if (eventGen instanceof SceneryEventGeneratorConfiguration)
				// we assume that there is only one per room
				return ((SceneryEventGeneratorConfiguration) eventGen).currentScenery;
		}
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

		if (this.serverName != serverName)
			return;

		this.roomConfig = config;

		List<AbstractRoomItemViewMapper> mappersToRefresh = new ArrayList<AbstractRoomItemViewMapper>(this.configuredlightObjects);

		this.configuredlightObjects.clear();

		for (AbstractRoomItemViewMapper currentToRefresh : mappersToRefresh) {
			ActorConfiguration currentConfig = roomConfig.actorConfigurations.get(currentToRefresh.getItemName());
			this.initRoomItemIconView(this.getCurrentScenery(), currentConfig, currentToRefresh.getLightObjectView());
		}

		// update switches
		for (EventGeneratorConfiguration currentEventGenerator : roomConfig.eventGeneratorConfigurations) {
			if (currentEventGenerator instanceof SwitchEventGeneratorConfiguration) {

				Switch currentSwitch = (Switch) this.roomContainerView.findViewWithTag("powerStateSwitch"
						+ currentEventGenerator.name);
				currentSwitch.setChecked(((SwitchEventGeneratorConfiguration) currentEventGenerator).getPowerState());

			}
		}

		updateRoomBackground();

		this.enableEventListener();

		// update widgets
		WidgetUtils.notifyWidgets(this.getActivity());

	}


	private RestClient getRestClient(){
		return ((MainActivity) this.getActivity()).getRestClient();
	}


	@Override
	public void onResume() {
		super.onResume();
		// this.onRoomConfigurationChange(this.serverName, this.roomConfig);
	}

}
