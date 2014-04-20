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

package org.ambient.control.processes;

import java.util.ArrayList;
import java.util.List;

import org.ambient.control.R;
import org.ambient.control.RoomServiceAwareFragment;
import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambient.control.config.EditConfigOnExitListener;
import org.ambient.control.processes.helper.SceneriesWrapper;
import org.ambient.rest.RestClient;
import org.ambient.util.GuiUtils;
import org.ambient.views.ProcessCardDrawer;
import org.ambient.views.ProcessCardDrawer.NodeSelectionListener;
import org.ambientlight.config.process.NodeConfiguration;
import org.ambientlight.config.process.ProcessConfiguration;
import org.ambientlight.config.room.entities.scenery.SceneryManagerConfiguration;
import org.ambientlight.room.entities.sceneries.Scenery;
import org.ambientlight.ws.Room;
import org.ambientlight.ws.process.validation.ValidationEntry;
import org.ambientlight.ws.process.validation.ValidationResult;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


/**
 * @author Florian Bornkessel
 * 
 */
public class ProcessCardFragment extends RoomServiceAwareFragment implements EditConfigOnExitListener {

	private static final String BUNDLE_SELECTED_PROCESS = "bundleSelectedProcess";
	private static final String BUNDLE_SELECTED_ROOM = "bundleSelectedRoom";
	private static final String BUNDLE_EDIT_MODE = "bundleEditMode";
	private static final String LOG = "ProcessCardFragment";

	private ActionMode mode = null;

	// int positionServer = 0;
	String selectedRoom = null;
	ProcessConfiguration selectedProcess = null;
	private boolean editMode = false;
	private final List<String> roomNames = new ArrayList<String>();
	private final List<String> processNames = new ArrayList<String>();

	View content;
	Spinner spinnerRoom;
	Spinner spinnerProcess;
	ArrayAdapter<String> processAdapter;
	ArrayAdapter<String> roomAdapter;
	ProcessCardDrawer drawer;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
		setHasOptionsMenu(true);

		this.content = inflater.inflate(R.layout.fragment_processcard, container, false);

		drawer = (ProcessCardDrawer) content.findViewById(R.id.processCardDrawer);
		spinnerRoom = (Spinner) content.findViewById(R.id.spinnerProcessRoom);
		spinnerProcess = (Spinner) content.findViewById(R.id.spinnerProcess);

		if (savedInstanceState != null) {
			selectedRoom = savedInstanceState.getString(BUNDLE_SELECTED_ROOM);
			selectedProcess = (ProcessConfiguration) savedInstanceState.getSerializable(BUNDLE_SELECTED_PROCESS);
			editMode = savedInstanceState.getBoolean(BUNDLE_EDIT_MODE);
		}

		return content;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.RoomServiceAwareFragment#onResumeWithServiceConnected ()
	 */
	@Override
	protected void onResumeWithServiceConnected() {
		spinnerRoom.setOnItemSelectedListener(null);
		selectedRoom = initRoomArrays(selectedRoom);
		drawer.setProcess(selectedProcess);

		roomAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, roomNames);
		roomAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

		processAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, processNames);
		processAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

		spinnerRoom.setAdapter(roomAdapter);
		spinnerProcess.setAdapter(processAdapter);

		if (editMode == true) {
			spinnerRoom.setVisibility(View.GONE);
			spinnerProcess.setVisibility(View.GONE);
			getActivity().invalidateOptionsMenu();
		}

		spinnerRoom.setSelection(roomNames.indexOf(selectedRoom));

		// create the listener after the initialisation. we do not want to
		// affect the second spinner while its in creation. let android this
		// for us (onResume, Screenrotation etc.)
		spinnerRoom.post(new Runnable() {

			@Override
			// TODO Auto-generated method stub
			public void run() {
				spinnerRoom.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
						editMode = false;
						getActivity().invalidateOptionsMenu();

						selectedRoom = roomNames.get(pos);
						Room selectedRoomConfiguration = roomService.getRoomConfiguration(selectedRoom);

						processNames.clear();
						for (ProcessConfiguration currentProcess : selectedRoomConfiguration.processManager.processes.values()) {
							processNames.add(currentProcess.id);
						}

						processAdapter.notifyDataSetChanged();
					}


					@Override
					public void onNothingSelected(AdapterView<?> paramAdapterView) {

					}
				});
			}
		});

		spinnerProcess.setSelection(getPositionOfSelectedProcess());

		spinnerProcess.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
				getActivity().invalidateOptionsMenu();

				Room selectedRoomConfiguration = roomService.getRoomConfiguration(selectedRoom);
				selectedProcess = (ProcessConfiguration) GuiUtils
						.deepCloneSerializeable(selectedRoomConfiguration.processManager.processes.get(parent.getSelectedItem()));
				drawer.setProcess(selectedProcess);
				getActivity().invalidateOptionsMenu();
			}


			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				getActivity().invalidateOptionsMenu();
			}
		});

		drawer.setOnNodeSelectionListener(new NodeSelectionListener() {

			@Override
			public void onNodeSelected(NodeConfiguration node) {
				if (mode != null) {
					mode.finish();
				}
				if (node == null)
					return;

				mode = createActionMode();

			}
		});

	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_processcard_menu, menu);
		if (editMode) {
			menu.findItem(R.id.menuEntryProcessRevertNew).setVisible(true);
			menu.findItem(R.id.menuEntryProcessRemove).setVisible(false);
			menu.findItem(R.id.menuEntryProcessSave).setVisible(true);

			menu.findItem(R.id.menuEntryProcessAdd).setVisible(false);
		} else {
			if (selectedProcess != null) {
				if (selectedProcess.run) {
					menu.findItem(R.id.menuEntryProcessStart).setVisible(false);
					menu.findItem(R.id.menuEntryProcessStop).setVisible(true);
				} else {
					menu.findItem(R.id.menuEntryProcessStart).setVisible(true);
					menu.findItem(R.id.menuEntryProcessStop).setVisible(false);
				}
			}
		}

	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menuEntryProcessStart:
			RestClient.startProcess(selectedRoom, selectedProcess.id);

			return true;

		case R.id.menuEntryProcessStop:
			RestClient.stopProcess(selectedRoom, selectedProcess.id);
			return true;

		case R.id.menuEntryProcessValidate:
			ValidationResult result = null;
			try {
				result = RestClient.validateProcess(selectedRoom, selectedProcess);
			} catch (Exception e) {
				Log.e(LOG, "could not validate process", e);
			}
			if (result.resultIsValid()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Validierung")
				.setMessage("Die Validierung ist abgeschlossen. Es wurden keine semantische Fehler gefunden.")
				.setPositiveButton("OK", null).create().show();
			} else {
				for (ValidationEntry entry : result.invalidateEntries) {
					drawer.addNodeWithError(selectedProcess.nodes.get(entry.nodeId), entry);
				}
			}
			return true;

		case R.id.menuEntryProcessAdd:
			try {
				EditConfigHandlerFragment.createNewConfigBean(ProcessConfiguration.class, this, selectedRoom,
						roomService.getRoomConfiguration(selectedRoom));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return true;

		case R.id.menuEntryProcessEdit:
			EditConfigHandlerFragment.editConfigBean(this, this.selectedProcess, this.selectedRoom,
					this.roomService.getRoomConfiguration(selectedRoom));
			return true;

		case R.id.menuEntryProcessRevertNew:
			// restore old process if possible. if not just load 1fst one in room.
			selectedProcess = roomService.getRoomConfiguration(selectedRoom).processManager.processes.get(selectedProcess.id);
			// maybe this could be done in onRoomConfigurationChange (at the moment the method does exactly this. but maybe this
			// changes in future - so this commend is a reminder for me
			getActivity().invalidateOptionsMenu();
			stopEditMode();
			return true;

		case R.id.menuEntryProcessSave:
			Boolean saveProcess = true;

			if (processNames.contains(selectedProcess.id)) {
				saveProcess = false;
			}

			if (saveProcess == false) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Prozess überschreiben").setMessage("Soll der bestehende Prozess überschrieben werden?")
				.setPositiveButton("OK", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						saveSelectedProcess();
					}
				}).setNegativeButton("Abbrechen", null).create().show();
			} else {
				saveSelectedProcess();
			}

			return true;

		case R.id.menuEntryProcessRemove:
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Prozess löschen").setMessage("Soll der Prozess gelöscht werden?")
			.setPositiveButton("OK", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						RestClient.deleteProcessFromRoom(selectedRoom, selectedProcess.id);
						selectedProcess = null;
					} catch (Exception e) {
						Log.e(LOG, "error deleting process", e);
					}
				}
			}).setNegativeButton("Abbrechen", null).create().show();
			return true;

		case R.id.menuEntryProcessSceneries:
			SceneriesWrapper sceneriesWrapper = new SceneriesWrapper();
			List<Scenery> sceneries = new ArrayList<Scenery>(
					roomService.getRoomConfiguration(selectedRoom).sceneriesManager.sceneries.values());
			sceneriesWrapper.sceneries = sceneries;

			EditConfigHandlerFragment.editConfigBean(this, sceneriesWrapper, selectedRoom,
					roomService.getRoomConfiguration(selectedRoom));

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	/**
	 * @return
	 */
	private int getPositionOfSelectedProcess() {
		int positionOfProcess = 0;
		if (selectedProcess != null) {
			positionOfProcess = this.processNames.indexOf(selectedProcess.id);
			if (positionOfProcess < 0) {
				positionOfProcess = 0;
			}
		}
		return positionOfProcess;
	}


	/**
	 * 
	 * @return
	 */
	public ActionMode createActionMode() {

		return getActivity().startActionMode(new ActionMode.Callback() {

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {

				case R.id.menuEntryProcessRemoveNode:
					// handling for nodes with a previous node in the process
					NodeConfiguration previousNode = null;
					for (NodeConfiguration currentPreviousNode : selectedProcess.nodes.values()) {
						if (currentPreviousNode.nextNodeIds != null
								&& currentPreviousNode.nextNodeIds.contains(drawer.getSelectedNode().id)) {
							previousNode = currentPreviousNode;
							break;
						}
					}

					if (previousNode != null) {
						int position = previousNode.nextNodeIds.indexOf(drawer.getSelectedNode().id);
						previousNode.nextNodeIds.remove(position);
						if (drawer.getSelectedNode().nextNodeIds.isEmpty() == false) {
							previousNode.nextNodeIds.add(position, drawer.getSelectedNode().nextNodeIds.get(0));
						}
						selectedProcess.nodes.remove(drawer.getSelectedNode().id);
					} else {
						// it is the first node in the process
						NodeConfiguration nextNode = selectedProcess.nodes.get(drawer.getSelectedNode().nextNodeIds.get(0));
						selectedProcess.nodes.remove(nextNode.id);
						nextNode.id = 0;
						selectedProcess.nodes.put(0, nextNode);
					}
					drawer.setProcess(selectedProcess);

					break;

				case R.id.menuEntryProcessAddSecondNode:
					NodeConfiguration secondNodeConfig = new NodeConfiguration();

					for (Integer i = 0; i <= selectedProcess.nodes.keySet().size(); i++) {
						if (selectedProcess.nodes.containsKey(i) == false) {
							secondNodeConfig.id = i;
							drawer.getSelectedNode().nextNodeIds.add(secondNodeConfig.id);
							selectedProcess.nodes.put(i, secondNodeConfig);
							drawer.setProcess(selectedProcess);
							drawer.setSelectdeNode(null);
							break;
						}
					}
					break;

				case R.id.menuEntryProcessAddNode:
					if (drawer.getSelectedNode().nextNodeIds.size() > 1) {
						// create dialog
					} else {
						NodeConfiguration nodeConfig = new NodeConfiguration();
						if (drawer.getSelectedNode().nextNodeIds.isEmpty() == false) {
							nodeConfig.nextNodeIds.add(drawer.getSelectedNode().nextNodeIds.get(0));
						}

						for (Integer i = 0; i <= selectedProcess.nodes.keySet().size(); i++) {
							if (selectedProcess.nodes.containsKey(i) == false) {
								nodeConfig.id = i;
								drawer.getSelectedNode().nextNodeIds.clear();
								drawer.getSelectedNode().nextNodeIds.add(nodeConfig.id);
								selectedProcess.nodes.put(i, nodeConfig);
								drawer.setProcess(selectedProcess);
								drawer.setSelectdeNode(null);
								break;
							}
						}
					}
					break;

				case R.id.menuEntryProcessEditNode:
					EditConfigHandlerFragment.editConfigBean(ProcessCardFragment.this, drawer.getSelectedNode(), selectedRoom,
							roomService.getRoomConfiguration(selectedRoom));
					break;

				default:
					break;

				}

				mode.finish();
				startEditMode();
				getActivity().invalidateOptionsMenu();
				return true;
			}


			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.fragment_processcard_cab, menu);

				MenuItem edit = menu.findItem(R.id.menuEntryProcessEditNode);
				MenuItem add = menu.findItem(R.id.menuEntryProcessAddNode);
				MenuItem addSecond = menu.findItem(R.id.menuEntryProcessAddSecondNode);
				MenuItem remove = menu.findItem(R.id.menuEntryProcessRemoveNode);
				if (drawer.getSelectedNode() != null) {
					edit.setVisible(true);
					add.setVisible(true);

					if (drawer.getSelectedNode().nextNodeIds.size() < 2) {
						remove.setVisible(true);
						if (drawer.getSelectedNode().nextNodeIds.size() == 1) {
							addSecond.setVisible(true);
						}
					}
				}
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
	}


	private void saveSelectedProcess() {
		try {

			ValidationResult result = RestClient.addProcess(selectedRoom, selectedProcess);

			if (result.resultIsValid() == false) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Fehler").setMessage("Es wurden Fehler gefunden. Der Prozess wurde nicht gespeichert.")
				.setPositiveButton("OK", null).create().show();
				for (ValidationEntry entry : result.invalidateEntries) {
					drawer.addNodeWithError(selectedProcess.nodes.get(entry.nodeId), entry);
				}
			} else {
				stopEditMode();
			}
		} catch (Exception e) {
			Log.e(LOG, "could not save process", e);
		}
	}


	private String initRoomArrays(String selectedRoom) {
		roomNames.clear();
		for (String serverName : roomService.getAllRoomConfigurationsMap().keySet()) {
			roomNames.add(roomService.getAllRoomConfigurationsMap().get(serverName).roomName);
		}

		if (selectedRoom == null) {
			selectedRoom = roomNames.get(0);
		}

		processNames.clear();
		for (ProcessConfiguration config : roomService.getRoomConfiguration(selectedRoom).processManager.processes.values()) {
			processNames.add(config.id);
		}
		if (roomAdapter != null) {
			roomAdapter.notifyDataSetChanged();
		}
		if (processAdapter != null) {
			processAdapter.notifyDataSetChanged();
		}
		return selectedRoom;
	}


	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(BUNDLE_SELECTED_ROOM, selectedRoom);
		savedInstanceState.putSerializable(BUNDLE_SELECTED_PROCESS, selectedProcess);
		savedInstanceState.putBoolean(BUNDLE_EDIT_MODE, editMode);
		super.onSaveInstanceState(savedInstanceState);

	}


	/*
	 * callback method is called everytime a configuration is edited.
	 * 
	 * @see org.ambient.control.processes.IntegrateObjectValueHandler# integrateConfiguration(java.lang.Object)
	 */
	@Override
	public void onIntegrateConfiguration(String roomName, Object configuration) {

		if (configuration instanceof ProcessConfiguration) {
			startEditMode();
			this.selectedProcess = (ProcessConfiguration) configuration;
			if (this.selectedProcess.nodes.isEmpty()) {
				NodeConfiguration node = new NodeConfiguration();
				this.selectedProcess.nodes.put(0, node);
			}
		}
		if (configuration instanceof NodeConfiguration) {
			startEditMode();
			NodeConfiguration nodeConfig = (NodeConfiguration) configuration;
			this.selectedProcess.nodes.put(nodeConfig.id, nodeConfig);
		}

		if (configuration instanceof SceneriesWrapper) {
			stopEditMode();

			// extract new sceneries
			SceneryManagerConfiguration sceneryManager = roomService.getRoomConfiguration(selectedRoom).sceneriesManager;
			List<Scenery> addNew = new ArrayList<Scenery>();
			for (Scenery current : ((SceneriesWrapper) configuration).sceneries) {
				if (sceneryManager.sceneries.containsKey(current.id) == false) {
					addNew.add(current);
				}
			}

			// create new sceneries on server
			try {
				for (Scenery current : addNew) {
					RestClient.createScenery(selectedRoom, current.id);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/*
	 * do not forget. if a different fragments thread calls this method the spinners will not be updated. therefore the boolean
	 * flag will signal onResumeWith...() to hide the spinners for itself in the right thread
	 */
	private void startEditMode() {
		spinnerRoom.setVisibility(View.GONE);
		spinnerProcess.setVisibility(View.GONE);
		this.editMode = true;
	}


	private void stopEditMode() {
		this.editMode = false;
		spinnerRoom.setVisibility(View.VISIBLE);
		spinnerProcess.setVisibility(View.VISIBLE);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.config.EditConfigExitListener#onRevertConfiguration (java.lang.String, java.lang.Object)
	 */
	@Override
	public void onRevertConfiguration(String roomName, Object configuration) {

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.RoomServiceAwareFragment#onRoomConfigurationChange (java.lang.String,
	 * org.ambientlight.room.RoomConfiguration)
	 */
	@Override
	public void onRoomConfigurationChange(String roomName, Room roomConfiguration) {
		if (roomName.equals(selectedRoom)) {
			initRoomArrays(selectedRoom);
			if (editMode == false) {

				spinnerProcess.setSelection(getPositionOfSelectedProcess());
				// for the case that the spinner has not changed its position we
				// will restore the process here
				selectedProcess = roomConfiguration.processManager.processes.get(selectedProcess.id);
				getActivity().invalidateOptionsMenu();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Änderung auf dem Server")
				.setMessage("Möglicherweise wurde der ursprüngliche Prozess im Hintergrund verändert.")
				.setPositiveButton("OK", null).create().show();
			}
		}
	}
}
