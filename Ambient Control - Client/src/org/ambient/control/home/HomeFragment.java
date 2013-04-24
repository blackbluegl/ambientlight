package org.ambient.control.home;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.ambient.control.DialogHolder;
import org.ambient.control.MainActivity;
import org.ambient.control.R;
import org.ambient.control.home.mapper.AbstractRoomItemViewMapper;
import org.ambient.control.home.mapper.SimpleColorLightItemViewMapper;
import org.ambient.control.home.mapper.SwitchItemViewMapper;
import org.ambient.control.home.mapper.TronLightItemViewMapper;
import org.ambient.control.rest.RestClient;
import org.ambient.control.sceneryconfiguration.ProgramChooserActivity;
import org.ambient.control.sceneryconfiguration.SimpleColorEditDialog;
import org.ambient.control.sceneryconfiguration.TronEditDialog;
import org.ambient.views.ImageViewWithContextMenuInfo;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;
import org.ambientlight.scenery.SceneryConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.TronRenderingProgrammConfiguration;
import org.ambientlight.scenery.switching.configuration.SwitchingConfiguration;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class HomeFragment extends Fragment implements HomeRefreshCallback {

	public static final String BUNDLE_HOST_LIST = "hosts";
	public static final String BUNDLE_SELECTED_ROOM_SERVER = "selectedRoomServer";
	private final int LIGHT_OBJECT_SIZE_DP = 85;

	private boolean mIsLargeLayout;
	
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
		mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

		String selectedRoomServer = getArguments().getString(BUNDLE_SELECTED_ROOM_SERVER);
		roomServers = getArguments().getStringArrayList(BUNDLE_HOST_LIST);

		this.myHomeView = (ViewGroup) inflater.inflate(R.layout.layout_home_main, container, false);
		LinearLayout roomList = (LinearLayout) myHomeView.findViewById(R.id.listHomeRooms);

		for (String currentRoomServer : roomServers) {
			try {
				boolean isCurrentRoomSelected = currentRoomServer.equals(selectedRoomServer);
				View result = this.initRoomView(inflater, currentRoomServer, isCurrentRoomSelected);
				roomList.addView(result);
			} catch (Exception e) {
				e.printStackTrace();
				// for now just ignore the room if it could not be build
			}
		}

		ImageView masterButton = (ImageView) myHomeView.findViewById(R.id.imageViewMasterSwitch);
		masterButton.setTag("masterButton");
		updateMasterSwitchState();
		masterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				for (String currentServerName : roomServers) {
					try {
						RestClient.setPowerStateForRoom(currentServerName, false);
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


	private View initRoomView(LayoutInflater inflater, final String serverName, boolean isCurrentRoomServerSelected)
			throws InterruptedException, ExecutionException {

		final RoomConfiguration roomConfig = RestClient.getRoom(serverName);

		// for the scenery save dialog to auto fill the current scenery name on
		// startup
		((MainActivity) getActivity()).setSelectedScenario(roomConfig.currentScenery);

		// create the room container
		final View roomContainerView = (View) inflater.inflate(R.layout.layout_room_item, null);

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
					RestClient.setPowerStateForRoom(serverName, powerButton.isChecked());
					setPowerStateForAllLightObjectsInRoom(powerButton.isChecked(), serverName);
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

				updateRoomPowerSwitchState(serverName);
				updateRoomBackground(serverName);
				updateMasterSwitchState();
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
				try {
					RestClient.setPowerStateForRoomItem(roomItemMapper.getServerName(), roomItemMapper.getItemName(),
							!roomItemMapper.getPowerState());
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

		SceneryConfiguration sceneryConfig = currentConfig.getSceneryConfigurationBySceneryName(sceneryName);

		if (sceneryConfig instanceof SimpleColorRenderingProgramConfiguration) {
			result = new SimpleColorLightItemViewMapper(lightObjectView, currentConfig.name, serverName,
					sceneryConfig.powerState, sceneryConfig.bypassOnSceneryChange);
		}

		if (sceneryConfig instanceof TronRenderingProgrammConfiguration) {
			result = new TronLightItemViewMapper(lightObjectView, currentConfig.name, serverName, sceneryConfig.powerState,
					sceneryConfig.bypassOnSceneryChange);
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
	public void refreshRoomContent(String roomServerName) throws InterruptedException, ExecutionException {

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
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.layout_room_item, menu);
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {

		ImageViewWithContextMenuInfo.ImageViewContextMenuInfo menuInfo = (ImageViewWithContextMenuInfo.ImageViewContextMenuInfo) item
				.getMenuInfo();
		ImageViewWithContextMenuInfo img = (ImageViewWithContextMenuInfo) menuInfo.targetView;
		AbstractRoomItemViewMapper mapper = (AbstractRoomItemViewMapper) img.getTag();

		String roomServer = mapper.getServerName();
		String lightObjectName = mapper.getItemName();
		String scenery = ((MainActivity) getActivity()).getSelectedScenario();
		switch (item.getItemId()) {

		case R.id.lightobject_context_bypass:
			try {
				RoomConfiguration rc = RestClient.getRoom(roomServer);
				RoomItemConfiguration roomItem = rc.getRoomItemConfigurationByName(lightObjectName);
				SceneryConfiguration sc = roomItem.getSceneryConfigurationBySceneryName(scenery);
				sc.bypassOnSceneryChange = !sc.bypassOnSceneryChange;
				RestClient.setProgramForLightObject(roomServer, scenery, lightObjectName, sc);
				mapper.setBypassSceneryChangeState(sc.bypassOnSceneryChange);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;

		case R.id.lightobject_context_edit:

			FragmentManager fm = getActivity().getSupportFragmentManager();
			DialogFragment dialog = null;
			if (mapper instanceof SimpleColorLightItemViewMapper) {
				dialog = new SimpleColorEditDialog();
			}
			if (mapper instanceof TronLightItemViewMapper) {
				dialog = new TronEditDialog();
			}
			Bundle args = new Bundle();
			args.putString("roomServer", roomServer);
			args.putString("lightObject", lightObjectName);
			args.putString("scenery", scenery);
			dialog.setArguments(args);

			// dialog.show(fm, "new Scenery Title");
			if (mIsLargeLayout) {
				// The device is using a large layout, so show the fragment as a
				// dialog
				dialog.show(fm, "Anpassen");
			} else {
				// The device is smaller, so show the fragment fullscreen
//				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				// For a little polish, specify a transition animation
//				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				// To make it fullscreen, use the 'content' root view as the
				// container
				// for the fragment, which is always the root view for the
				// activity
//				transaction.add(R.id.LayoutMain, dialog).addToBackStack(null).commit();
				Intent i = new Intent(getActivity(),DialogHolder.class);
				i.putExtras(args);
				i.putExtra("dialog",mapper.getClass().getSimpleName());
				startActivity(i);
			}

			return true;
		case R.id.lightobject_context_new:

			Intent i2 = new Intent(getActivity(), ProgramChooserActivity.class);
			i2.putExtra("roomServer", roomServer);
			i2.putExtra("lightObject", lightObjectName);
			startActivity(i2);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
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
}
