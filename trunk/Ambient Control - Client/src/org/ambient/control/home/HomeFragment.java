package org.ambient.control.home;

import java.util.ArrayList;
import java.util.List;

import org.ambient.control.MainActivity;
import org.ambient.control.R;
import org.ambient.control.home.mapper.AbstractRoomItemViewMapper;
import org.ambient.control.home.mapper.SimpleColorLightItemViewMapper;
import org.ambient.control.home.mapper.SwitchItemViewMapper;
import org.ambient.control.home.mapper.TronLightItemViewMapper;
import org.ambient.control.rest.RestClient;
import org.ambient.control.sceneryconfiguration.SceneryConfigEditDialogFragment;
import org.ambient.control.sceneryconfiguration.SceneryConfigEditDialogHolder;
import org.ambient.control.sceneryconfiguration.SceneryProgramChooserActivity;
import org.ambient.util.GuiUtils;
import org.ambient.views.ImageViewWithContextMenuInfo;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;
import org.ambientlight.scenery.EntitiyConfiguration;
import org.ambientlight.scenery.actor.rendering.programms.configuration.TronRenderingProgrammConfiguration;
import org.ambientlight.scenery.actor.switching.configuration.SwitchingConfiguration;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class HomeFragment extends Fragment implements HomeRefreshCallback {

	public static final String BUNDLE_HOST_LIST = "hosts";
	public static final String BUNDLE_SELECTED_ROOM_SERVER = "selectedRoomServer";
	private final int LIGHT_OBJECT_SIZE_DP = 85;

	/*
	 * list of all roomServers which will be represented by a roomContainer
	 * within this fragment. The initialization is handled by a bundle.
	 */
	private List<String> roomServers;

	/*
	 * the lightobjectMappers will store information about the configuration of
	 * an lightobject like name, state and type. reinit is handled within the
	 * fragment
	 */
	private List<AbstractRoomItemViewMapper> configuredlightObjects = new ArrayList<AbstractRoomItemViewMapper>();

	private View myHomeView;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		String selectedRoomServer = getArguments().getString(BUNDLE_SELECTED_ROOM_SERVER);
		roomServers = getArguments().getStringArrayList(BUNDLE_HOST_LIST);

		this.myHomeView = (ViewGroup) inflater.inflate(R.layout.layout_home_main, container, false);
		LinearLayout roomList = (LinearLayout) myHomeView.findViewById(R.id.listHomeRooms);

		final HomeRefreshCallback callback = this;
		
		List<String> roomServersToDelete = new ArrayList<String>();
		for (String currentRoomServer : roomServers) {
			try {
				
				boolean isCurrentRoomSelected = currentRoomServer.equals(selectedRoomServer);
				View result = this.initRoomView(inflater, currentRoomServer, isCurrentRoomSelected, callback);
				if(result !=null){
					roomList.addView(result);
					((MainActivity)this.getActivity()).selectedRoomServer=currentRoomServer;
				}
				else{
					roomServersToDelete.add(currentRoomServer);
				}
			} catch (Exception e) {
				e.printStackTrace();
				// for now just ignore the room if it could not be build
			}	
		}
		this.roomServers.removeAll(roomServersToDelete);
		
		ImageView masterButton = (ImageView) myHomeView.findViewById(R.id.imageViewMasterSwitch);
		masterButton.setTag("masterButton");
		updateMasterSwitchState();
		masterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				for (String currentServerName : roomServers) {
					try {
						RestClient.setPowerStateForRoom(currentServerName, false, callback);
					} catch (Exception e) {
						e.printStackTrace();
					}
					setPowerStateForAllLightObjectsInRoom(false, currentServerName);
					updateRoomPowerSwitchState(currentServerName);
					updateRoomBackground(currentServerName);
				}
				updateMasterSwitchState();
			}
		});

		return myHomeView;
	}


	private View initRoomView(LayoutInflater inflater, final String serverName, boolean isCurrentRoomServerSelected, final HomeRefreshCallback callback)
			throws Exception {

		final RoomConfiguration roomConfig = RestClient.getRoom(serverName);
		
		if(roomConfig == null){
			return null;
		}
		
		// for the scenery save dialog to auto fill the current scenery name on
		// startup
		((MainActivity) getActivity()).setSelectedScenario(roomConfig.currentScenery);

		// create the room container
		final View roomContainerView = (View) inflater.inflate(R.layout.layout_room_item, null);
		roomContainerView.setTag("roomContainer"+serverName);
		TableLayout roomContent = (TableLayout) roomContainerView.findViewById(R.id.roomContent);
		roomContent.setTag("roomContent" + serverName);

		TextView roomLabel = (TextView) roomContainerView.findViewById(R.id.textViewRoomName);
		roomLabel.setText(roomConfig.roomName);
		roomLabel.setTag(serverName + "roomLabel");
		if (isCurrentRoomServerSelected) {
			roomLabel.setTextAppearance(this.getActivity(), R.style.boldRoomLabel);
		} else {
			roomLabel.setTextAppearance(this.getActivity(), R.style.normalRoomLabel);
		}

		// a click on any element in the room informs the sceneryChooser to load
		// the corresponding scenery list for that room
		roomContainerView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// first set all roomlabels inactive
				for (String currentRoomServer : roomServers) {
					setRoomLabelSelected(currentRoomServer, false);
				}

				// activate current roomlabel
				setRoomLabelSelected(serverName, true);
				if (serverName.equals(((MainActivity) getActivity()).getSelectedRoomServer()) == false) {
					((MainActivity) getActivity()).updateSceneriesForSelectedRoomServer(serverName);
				}
			}
		});

		TextView sceneryLabel = (TextView) roomContainerView.findViewById(R.id.textViewSceneryName);
		sceneryLabel.setText(roomConfig.currentScenery);
		sceneryLabel.setTag(serverName + "sceneryLabel");

		// init dynamically the clickable light object icons
		int amountPerRow = getLightObjectAmountPerRow();

		TableRow row = new TableRow(roomContent.getContext());
		row.setGravity(Gravity.CENTER);
		roomContent.addView(row);

		for (int i = 0; i < roomConfig.roomItemConfigurations.size(); i++) {

			// create a new row if last one is full
			if (i % amountPerRow == 0) {
				row = new TableRow(roomContent.getContext());
				row.setGravity(Gravity.CENTER);
				roomContent.addView(row);
			}

			View lightObject = (View) inflater.inflate(R.layout.layout_room_lightobject, null);
			RoomItemConfiguration currentRoomItemConfiguration = roomConfig.roomItemConfigurations.get(i);
			initRoomItemIconView(serverName, roomConfig.currentScenery, currentRoomItemConfiguration, lightObject);

			row.addView(lightObject);
		}

		// init room power switch dynamically
		LinearLayout roomBottomBarView = (LinearLayout) roomContainerView.findViewById(R.id.roomBottomBar);

		Switch powerStateSwitch = new Switch(this.getActivity());
		powerStateSwitch.setTag("powerStateSwitch" + serverName);
		roomBottomBarView.addView(powerStateSwitch, 0);
		powerStateSwitch.setChecked(this.getPowerStateForAllLightObjectsInRoom(serverName));
		powerStateSwitch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Switch powerButton = (Switch) v;
				try {
					disableEventListener(serverName);
					RestClient.setPowerStateForRoom(serverName, powerButton.isChecked(), callback);
					//setPowerStateForAllLightObjectsInRoom(powerButton.isChecked(), serverName);
					//refreshRoomContent(serverName);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// set all roomlabels inactive
				for (String currentRoomServer : roomServers) {
					setRoomLabelSelected(currentRoomServer, false);
				}

				// activate current roomlabel
				setRoomLabelSelected(serverName, true);
				if (serverName.equals(((MainActivity) getActivity()).getSelectedRoomServer()) == false) {
					((MainActivity) getActivity()).updateSceneriesForSelectedRoomServer(serverName);
				}

//				updateRoomPowerSwitchState(serverName);
//				updateRoomBackground(serverName);
//				updateMasterSwitchState();
			}
		});

		return roomContainerView;
	}


	private void initRoomItemIconView(final String serverName, String sceneryName, final RoomItemConfiguration currentConfig,
			View lightObjectView) {

		final AbstractRoomItemViewMapper roomItemMapper = getLightObjectMapperForLightObjectIcon(serverName, sceneryName,
				currentConfig, lightObjectView);

		this.configuredlightObjects.add(roomItemMapper);

		ImageViewWithContextMenuInfo icon = (ImageViewWithContextMenuInfo) lightObjectView
				.findViewById(R.id.imageViewLightObject);
		icon.setTag(roomItemMapper);
		registerForContextMenu(icon);

		// a click on an icon toggles the powerstate on the server
		icon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//while updateing the roomContent the Icons will be disabled
				if(roomItemMapper.isEventListenerDisabled()){
					return;
				}
				
				try {
					RestClient.setPowerStateForRoomItem(roomItemMapper.getServerName(), roomItemMapper.getItemName(),
							!roomItemMapper.getPowerState());
					//updateing the icon
					roomItemMapper.setPowerState(!roomItemMapper.getPowerState());

					// set all roomlabels inactive
					for (String currentRoomServer : roomServers) {
						setRoomLabelSelected(currentRoomServer, false);
					}

					// activate current roomlabel
					setRoomLabelSelected(serverName, true);
					if (serverName.equals(((MainActivity) getActivity()).getSelectedRoomServer()) == false) {
						((MainActivity) getActivity()).updateSceneriesForSelectedRoomServer(serverName);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				updateRoomPowerSwitchState(serverName);
				updateRoomBackground(serverName);
				updateMasterSwitchState();
			}
		});
	}


	private AbstractRoomItemViewMapper getLightObjectMapperForLightObjectIcon(final String serverName, String sceneryName,
			final RoomItemConfiguration currentConfig, View lightObjectView) {
		AbstractRoomItemViewMapper result = null;

		EntitiyConfiguration sceneryConfig = currentConfig.getSceneryConfigurationBySceneryName(sceneryName);

		if (sceneryConfig instanceof org.ambientlight.scenery.actor.rendering.programms.configuration.SimpleColorRenderingProgramConfiguration) {
			result = new SimpleColorLightItemViewMapper(lightObjectView, currentConfig.name, R.string.program_simple_color,
					serverName, sceneryConfig.powerState, sceneryConfig.bypassOnSceneryChange);
		}

		if (sceneryConfig instanceof TronRenderingProgrammConfiguration) {
			result = new TronLightItemViewMapper(lightObjectView, currentConfig.name, R.string.program_tron, serverName,
					sceneryConfig.powerState, sceneryConfig.bypassOnSceneryChange);
		}

		if (sceneryConfig instanceof SwitchingConfiguration) {
			result = new SwitchItemViewMapper(lightObjectView, currentConfig.name, serverName, sceneryConfig.powerState,
					sceneryConfig.bypassOnSceneryChange);
		}

		result.setBypassSceneryChangeState(sceneryConfig.bypassOnSceneryChange);

		return result;
	}


	private int getLightObjectAmountPerRow() {
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		float dp = getResources().getDisplayMetrics().widthPixels;

		final float scale = getResources().getDisplayMetrics().density;
		int containerSize = (int) (dp / scale + 0.5f);

		int amountPerRow = containerSize / this.LIGHT_OBJECT_SIZE_DP;
		return amountPerRow;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambient.control.home.HomeRefreshCallback#refreshRoomContent(java.
	 * lang.String)
	 */
	@Override
	public void refreshRoomContent(String roomServerName) throws Exception {

		List<AbstractRoomItemViewMapper> removeMappers = new ArrayList<AbstractRoomItemViewMapper>();

		for (AbstractRoomItemViewMapper current : this.configuredlightObjects) {
			if (current.getServerName().equals(roomServerName)) {
				removeMappers.add(current);
			}
		}

		this.configuredlightObjects.removeAll(removeMappers);

		RoomConfiguration roomConfiguration = RestClient.getRoom(roomServerName);

		// for the scenery save dialog to auto fill the current scenery name on
		// scenery change
		((MainActivity) getActivity()).setSelectedScenario(roomConfiguration.currentScenery);

		for (AbstractRoomItemViewMapper currentToRefresh : removeMappers) {
			RoomItemConfiguration currentConfig = roomConfiguration
					.getRoomItemConfigurationByName(currentToRefresh.getItemName());
			this.initRoomItemIconView(roomServerName, roomConfiguration.currentScenery, currentConfig,
					currentToRefresh.getLightObjectView());
		}

		updateRoomPowerSwitchState(roomServerName);
		updateRoomBackground(roomServerName);
		updateMasterSwitchState();

		TextView sceneryLabel = (TextView) this.myHomeView.findViewWithTag(roomServerName + "sceneryLabel");
		sceneryLabel.setText(roomConfiguration.currentScenery);
		
		this.enableEventListener(roomServerName);
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();

		AbstractRoomItemViewMapper mapper = (AbstractRoomItemViewMapper) v.getTag();
		if (mapper instanceof SwitchItemViewMapper) {
			inflater.inflate(R.menu.layout_switch_object, menu);
		} else {
			inflater.inflate(R.menu.layout_light_object, menu);
		}
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {

		//no room item clicked. there is nothing todo here for us
		if(item.getMenuInfo() instanceof ImageViewWithContextMenuInfo.ImageViewContextMenuInfo == false){
		    return super.onContextItemSelected(item); 	
		}
		
		ImageViewWithContextMenuInfo.ImageViewContextMenuInfo menuInfo = (ImageViewWithContextMenuInfo.ImageViewContextMenuInfo) item
				.getMenuInfo();
		ImageViewWithContextMenuInfo img = (ImageViewWithContextMenuInfo) menuInfo.targetView;
		AbstractRoomItemViewMapper mapper = (AbstractRoomItemViewMapper) img.getTag();

		String roomServer = mapper.getServerName();
		String lightObjectName = mapper.getItemName();
		String scenery = ((MainActivity) getActivity()).getSelectedScenario();

		Bundle args = new Bundle();
		args.putString("roomServer", roomServer);
		args.putString("lightObject", lightObjectName);
		args.putString("scenery", scenery);
		args.putString("title", "Eigenschaften");

		switch (item.getItemId()) {

		case R.id.lightobject_context_bypass:
			try {
				RoomConfiguration rc = RestClient.getRoom(roomServer);
				RoomItemConfiguration roomItem = rc.getRoomItemConfigurationByName(lightObjectName);
				EntitiyConfiguration sc = roomItem.getSceneryConfigurationBySceneryName(scenery);
				sc.bypassOnSceneryChange = !sc.bypassOnSceneryChange;
				RestClient.setProgramForLightObject(roomServer, scenery, lightObjectName, sc);
				mapper.setBypassSceneryChangeState(sc.bypassOnSceneryChange);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;

		case R.id.lightobject_context_edit:

			FragmentManager fm = getActivity().getSupportFragmentManager();
			SceneryConfigEditDialogFragment editSceneryConfigFragment = new SceneryConfigEditDialogFragment();

			editSceneryConfigFragment.setArguments(args);

			if (GuiUtils.isLargeLayout(getActivity())) {
				// The device is using a large layout, so show the fragment as a
				// dialog
				editSceneryConfigFragment.show(fm, null);
			} else {
				// The screen is smaller, so show the fragment in a fullscreen
				// activity
				Intent i = new Intent(getActivity(), SceneryConfigEditDialogHolder.class);
				i.putExtras(args);
				i.putExtra("resourceId", mapper.getResourceId());
				startActivity(i);
			}
			return true;

		case R.id.lightobject_context_new:

			Intent i = new Intent(getActivity(), SceneryProgramChooserActivity.class);
			i.putExtras(args);
			startActivity(i);
			return true;
		}
		
	    return super.onContextItemSelected(item); 
	}


	private void updateMasterSwitchState() {

		ImageView masterSwitch = (ImageView) this.myHomeView.findViewWithTag("masterButton");

		for (AbstractRoomItemViewMapper current : this.configuredlightObjects) {
			if (current.getPowerState() == true) {
				masterSwitch.setImageResource(R.drawable.ic_power_active);
				return;
			}
		}
		masterSwitch.setImageResource(R.drawable.ic_power_disabled);
	}


	private void updateRoomPowerSwitchState(String serverName) {
		Switch powerStateSwitch = (Switch) this.myHomeView.findViewWithTag("powerStateSwitch" + serverName);
		powerStateSwitch.setChecked(this.getPowerStateForAllLightObjectsInRoom(serverName));
	}


	private void updateRoomBackground(String serverName) {
		Switch powerStateSwitch = (Switch) this.myHomeView.findViewWithTag("powerStateSwitch" + serverName);
		TableLayout roomContent = (TableLayout) this.myHomeView.findViewWithTag("roomContent" + serverName);

		boolean powerSwitchIsChecked = powerStateSwitch.isChecked();

		for (AbstractRoomItemViewMapper current : this.configuredlightObjects) {
			if (current.getServerName().equals(serverName) && current.getPowerState() != powerSwitchIsChecked) {
				roomContent.setBackgroundResource(R.drawable.bg_room_half_active);
				return;
			}
		}

		if (this.getPowerStateForAllLightObjectsInRoom(serverName)) {
			roomContent.setBackgroundResource(R.drawable.bg_room_active);
		} else {
			roomContent.setBackgroundResource(R.drawable.bg_room_disabled);
		}
	}


	private void setPowerStateForAllLightObjectsInRoom(boolean isActive, String serverName) {
		for (AbstractRoomItemViewMapper current : this.configuredlightObjects) {
			if (current.getServerName().equals(serverName)) {
				current.setPowerState(isActive);
			}
		}
	}


	private boolean getPowerStateForAllLightObjectsInRoom(String serverName) {
		for (AbstractRoomItemViewMapper current : this.configuredlightObjects) {
			if (current.getServerName().equals(serverName) && current.getPowerState() == true) {
				return true;
			}
		}
		return false;
	}


	private void setRoomLabelSelected(String serverName, boolean selected) {
		TextView roomLabel = (TextView) this.myHomeView.findViewWithTag(serverName + "roomLabel");

		if (selected) {
			roomLabel.setTextAppearance(this.getActivity(), R.style.boldRoomLabel);
		} else {
			roomLabel.setTextAppearance(this.getActivity(), R.style.normalRoomLabel);
		}
	}
	
	private void disableEventListener(String serverName){
		LinearLayout roomList = (LinearLayout) myHomeView.findViewById(R.id.listHomeRooms);
		View room = roomList.findViewWithTag("roomContainer"+serverName);
		ProgressBar bar = (ProgressBar) room.findViewById(R.id.progressBar);
		bar.setVisibility(ProgressBar.VISIBLE);
		TableLayout roomContent = (TableLayout) room.findViewWithTag("roomContent" + serverName);
		roomContent.setVisibility(TableLayout.GONE);
		
		Switch powerStateSwitch = (Switch) room.findViewWithTag("powerStateSwitch" + serverName);
		powerStateSwitch.setVisibility(Switch.INVISIBLE);
		for(AbstractRoomItemViewMapper mapper : configuredlightObjects){
			mapper.setEventListenerDisabled(true);
		}
	}
	
	private void enableEventListener(String serverName){
		LinearLayout roomList = (LinearLayout) myHomeView.findViewById(R.id.listHomeRooms);
		View room = roomList.findViewWithTag("roomContainer"+serverName);
		ProgressBar bar = (ProgressBar) room.findViewById(R.id.progressBar);
		bar.setVisibility(ProgressBar.GONE);
		
		TableLayout roomContent = (TableLayout) room.findViewWithTag("roomContent" + serverName);
		roomContent.setVisibility(TableLayout.VISIBLE);
		
		Switch powerStateSwitch = (Switch) room.findViewWithTag("powerStateSwitch" + serverName);
		powerStateSwitch.setVisibility(Switch.VISIBLE);
		
		for(AbstractRoomItemViewMapper mapper : configuredlightObjects){
			mapper.setEventListenerDisabled(false);
		}
	}
}
