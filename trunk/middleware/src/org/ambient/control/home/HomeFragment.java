package org.ambient.control.home;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.ambient.control.MainActivity;
import org.ambient.control.R;
import org.ambient.control.home.mapper.AbstractLightObjectIconMapper;
import org.ambient.control.home.mapper.SimpleColorLightObjectMapper;
import org.ambient.control.programs.ProgramChooser;
import org.ambient.control.programs.ProgramEditorActivity;
import org.ambient.control.rest.RestClient;
import org.ambient.views.ImageViewWithContextMenuInfo;
import org.ambientlight.scenery.entities.configuration.LightObjectConfiguration;
import org.ambientlight.scenery.entities.configuration.RoomConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.SimpleColorRenderingProgramConfiguration;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class HomeFragment extends Fragment {

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
	private List<AbstractLightObjectIconMapper> configuredlightObjects = new ArrayList<AbstractLightObjectIconMapper>();

	private View myHomeView;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
					setPowerStateForAllLightObjectsInRoom(false, currentServerName);
					updateRoomPowerSwitchState(currentServerName);
					updateRoomBackground(currentServerName);
				}
				updateMasterSwitchState();
			}
		});
		
		return myHomeView;
	}

	
	private View initRoomView(LayoutInflater inflater, final String serverName, boolean isCurrentRoomServerSelected) throws InterruptedException, ExecutionException {
		
		final RoomConfiguration roomConfig = RestClient.getRoom(serverName);

		//create the room container
		final View roomContainerView = (View) inflater.inflate(R.layout.layout_room_item, null);
		
		TableLayout roomContent = (TableLayout) roomContainerView.findViewById(R.id.roomContent);
		roomContent.setTag("roomContent" + serverName);

		TextView roomLabel = (TextView) roomContainerView.findViewById(R.id.textViewRoomName);
		roomLabel.setText(roomConfig.roomName);
		roomLabel.setTag(serverName+"roomLabel");
		if(isCurrentRoomServerSelected){
			roomLabel.setTextAppearance(this.getActivity(), R.style.boldRoomLabel);
		}else{
			roomLabel.setTextAppearance(this.getActivity(), R.style.normalRoomLabel);
		}
		
		// a click on any element in the room informs the sceneryChooser to load
		// the corresponding scenery list for that room
		roomContainerView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//first set all roomlabels inactive
				for(String currentRoomServer : roomServers){
					setRoomLabelSelected(currentRoomServer, false);
				}

				//activate current roomlabel
				setRoomLabelSelected(serverName, true);
				if (serverName.equals(((MainActivity) getActivity()).getSelectedRoomServer()) == false) {
					((MainActivity) getActivity()).updateSceneriesForSelectedRoomServer(serverName);
				}
			}
		});		
		
		TextView sceneryLabel = (TextView) roomContainerView.findViewById(R.id.textViewSceneryName);
		sceneryLabel.setText(roomConfig.lightObjects.get(0).currentRenderingProgrammConfiguration.sceneryName);
		
		//init dynamically the clickable light object icons
		int amountPerRow = getLightObjectAmountPerRow();
		
		TableRow row = new TableRow(roomContent.getContext());
		row.setGravity(Gravity.CENTER);
		roomContent.addView(row);

		for (int i = 0; i < roomConfig.lightObjects.size(); i++) {
			
			//create a new row if last one is full
			if (i % amountPerRow == 0) {
				row = new TableRow(roomContent.getContext());
				row.setGravity(Gravity.CENTER);
				roomContent.addView(row);
			}
			
			View lightObject = (View) inflater.inflate(R.layout.layout_room_lightobject, null);
			LightObjectConfiguration currentLightObjectConfig = roomConfig.lightObjects.get(i);
			initLightObjectIconView(serverName, currentLightObjectConfig, lightObject);

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

				updateRoomPowerSwitchState(serverName);
				updateRoomBackground(serverName);
				updateMasterSwitchState();
			}
		});
		
		return roomContainerView;
	}

	
	private void initLightObjectIconView(final String serverName, final LightObjectConfiguration currentConfig, View lightObjectView) {

		final AbstractLightObjectIconMapper lightObjectMapper = getLightObjectMapperForLightObjectIcon
				(serverName, currentConfig, lightObjectView);
		
		this.configuredlightObjects.add(lightObjectMapper);
		
		ImageViewWithContextMenuInfo icon = (ImageViewWithContextMenuInfo) lightObjectView.findViewById(R.id.imageViewLightObject);
		icon.setTag(lightObjectMapper);
		registerForContextMenu(icon);

		//a click on an icon toggles the powerstate on the server
		icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					RestClient.setPowerStateForLightObject(lightObjectMapper.getServerName(),
							lightObjectMapper.getLightObjectName(), !lightObjectMapper.getPowerState());
					lightObjectMapper.setPowerState(!lightObjectMapper.getPowerState());
				} catch (Exception e) {
					e.printStackTrace();
				}

				updateRoomPowerSwitchState(serverName);
				updateRoomBackground(serverName);
				updateMasterSwitchState();
			}
		});
	}


	private AbstractLightObjectIconMapper getLightObjectMapperForLightObjectIcon(final String serverName,
			final LightObjectConfiguration currentConfig, View lightObjectView) {
		AbstractLightObjectIconMapper result = null;

		if (currentConfig.currentRenderingProgrammConfiguration instanceof SimpleColorRenderingProgramConfiguration) {
			result = new SimpleColorLightObjectMapper(lightObjectView, currentConfig.lightObjectName, serverName,
					currentConfig.currentRenderingProgrammConfiguration.powerState);
		}
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


	public void refreshRoomContent(String roomServerName) throws InterruptedException, ExecutionException {

		List<AbstractLightObjectIconMapper> removeMappers = new ArrayList<AbstractLightObjectIconMapper>();

		for (AbstractLightObjectIconMapper current : this.configuredlightObjects) {
			if (current.getServerName().equals(roomServerName)) {
				removeMappers.add(current);
			}
		}

		this.configuredlightObjects.removeAll(removeMappers);

		RoomConfiguration roomConfiguration = RestClient.getRoom(roomServerName);

		for (AbstractLightObjectIconMapper currentToRefresh : removeMappers) {
			LightObjectConfiguration currentConfig = roomConfiguration.getLightObjectConfigurationByName(currentToRefresh.getLightObjectName());
			this.initLightObjectIconView(roomServerName, currentConfig, currentToRefresh.getLightObjectView());
		}

		updateRoomPowerSwitchState(roomServerName);
		updateRoomBackground(roomServerName);
		updateMasterSwitchState();
		
		TextView sceneryLabel = (TextView) this.myHomeView.findViewWithTag(roomServerName+"roomLabel");
		sceneryLabel.setText(roomConfiguration.lightObjects.get(0).currentRenderingProgrammConfiguration.sceneryName);
	}
	

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.layout_room_item, menu);		
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		ImageViewWithContextMenuInfo.ImageViewContextMenuInfo menuInfo = (ImageViewWithContextMenuInfo.ImageViewContextMenuInfo) item.getMenuInfo();  
	    ImageViewWithContextMenuInfo img = (ImageViewWithContextMenuInfo) menuInfo.targetView;  
		AbstractLightObjectIconMapper mapper = (AbstractLightObjectIconMapper) img.getTag();
		
		String roomServer = mapper.getServerName();
		String lightObjectName = mapper.getLightObjectName();
		
		switch (item.getItemId()) {
		case R.id.lightobject_context_edit:
			Intent i = new Intent(getActivity(), ProgramEditorActivity.class);
			i.putExtra("roomServer", roomServer);
			i.putExtra("lightObject", lightObjectName);
			startActivity(i);
			return true;
		case R.id.lightobject_context_new:

			Intent i2 = new Intent(getActivity(), ProgramChooser.class);
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

		for (AbstractLightObjectIconMapper current : this.configuredlightObjects) {
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

		for (AbstractLightObjectIconMapper current : this.configuredlightObjects) {
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
		for (AbstractLightObjectIconMapper current : this.configuredlightObjects) {
			if (current.getServerName().equals(serverName)) {
				current.setPowerState(isActive);
			}
		}
	}

	private boolean getPowerStateForAllLightObjectsInRoom(String serverName) {
		for (AbstractLightObjectIconMapper current : this.configuredlightObjects) {
			if (current.getServerName().equals(serverName) && current.getPowerState() == true) {
				return true;
			}
		}
		return false;
	}
	
	private void setRoomLabelSelected(String serverName, boolean selected) {
		TextView roomLabel = (TextView)this.myHomeView.findViewWithTag(serverName+"roomLabel");
		
		if(selected){
			roomLabel.setTextAppearance(this.getActivity(), R.style.boldRoomLabel);
		}else{
			roomLabel.setTextAppearance(this.getActivity(), R.style.normalRoomLabel);
		}
	}
}
