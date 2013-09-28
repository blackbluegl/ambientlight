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
import org.ambient.control.config.EditConfigExitListener;
import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambient.control.processes.helper.SceneriesWrapper;
import org.ambient.control.rest.RestClient;
import org.ambient.util.GuiUtils;
import org.ambient.views.ProcessCardDrawer;
import org.ambient.views.ProcessCardDrawer.NodeSelectionListener;
import org.ambientlight.process.NodeConfiguration;
import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.process.validation.ValidationEntry;
import org.ambientlight.process.validation.ValidationResult;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.eventgenerator.SceneryEventGeneratorConfiguration;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
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
public class ProcessCardFragment extends RoomServiceAwareFragment implements EditConfigExitListener {

	private static final String BUNDLE_SELECTED_PROCESS = "bundleSelectedProcess";
	private static final String BUNDLE_SELECTED_SERVER = "bundleSelectedServer";
	private static final String BUNDLE_EDIT_MODE = "bundleEditMode";
	private static final String LOG = "ProcessCardFragment";

	private ActionMode mode = null;

	// int positionServer = 0;
	String selectedServer = null;
	ProcessConfiguration selectedProcess = null;
	private boolean editMode = false;
	private final List<String> serverNames = new ArrayList<String>();
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
			selectedServer = savedInstanceState.getString(BUNDLE_SELECTED_SERVER);
			selectedProcess = (ProcessConfiguration) savedInstanceState.getSerializable(BUNDLE_SELECTED_PROCESS);
			editMode = savedInstanceState.getBoolean(BUNDLE_EDIT_MODE);
		}

		return content;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambient.control.RoomServiceAwareFragment#onResumeWithServiceConnected
	 * ()
	 */
	@Override
	protected void onResumeWithServiceConnected() {
		spinnerRoom.setOnItemSelectedListener(null);
		selectedServer = initRoomArrays(selectedServer);
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

		spinnerRoom.setSelection(serverNames.indexOf(selectedServer));

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

						selectedServer = serverNames.get(pos);
						RoomConfiguration selectedRoomConfiguration = roomService.getRoomConfiguration(selectedServer);

						processNames.clear();
						for (ProcessConfiguration currentProcess : selectedRoomConfiguration.processes) {
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
				RoomConfiguration selectedRoomConfiguration = roomService.getRoomConfiguration(selectedServer);
				selectedProcess = (ProcessConfiguration) GuiUtils.deepCloneSerializeable(selectedRoomConfiguration.processes
						.get(pos));
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
			RestClient.startProcess(selectedServer, selectedProcess.id);

			return true;

		case R.id.menuEntryProcessStop:
			RestClient.stopProcess(selectedServer, selectedProcess.id);
			return true;

		case R.id.menuEntryProcessValidate:
			ValidationResult result = null;
			try {
				result = RestClient.validateProcess(selectedServer, selectedProcess);
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
			EditConfigHandlerFragment.createNewConfigBean(ProcessConfiguration.class, this, selectedServer,
					roomService.getRoomConfiguration(selectedServer));
			return true;

		case R.id.menuEntryProcessEdit:
			EditConfigHandlerFragment fragEdit = new EditConfigHandlerFragment();
			fragEdit.setTargetFragment(this, EditConfigHandlerFragment.REQ_RETURN_OBJECT);
			Bundle arguments = new Bundle();
			arguments.putSerializable(EditConfigHandlerFragment.OBJECT_VALUE, this.selectedProcess);
			arguments.putBoolean(EditConfigHandlerFragment.CREATE_MODE, false);
			arguments.putString(EditConfigHandlerFragment.SELECTED_SERVER, selectedServer);
			fragEdit.setArguments(arguments);
			FragmentTransaction ft2 = getFragmentManager().beginTransaction();
			ft2.replace(R.id.LayoutMain, fragEdit);
			ft2.addToBackStack(null);
			ft2.commit();
			return true;

		case R.id.menuEntryProcessRevertNew:
			// restore old process if possible. if not just load 1fst one in
			// room.
			selectedProcess = roomService.getRoomConfiguration(selectedServer).processes.get(getPositionOfSelectedProcess());
			// maybe this could be done in onRoomConfigurationChange (at the
			// moment the method does exactly this. but maybe this changes in
			// future - so this commend is a reminder for me
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
						RestClient.deleteProcessFromRoom(selectedServer, selectedProcess.id);
						selectedProcess = null;
					} catch (Exception e) {
						Log.e(LOG, "error deleting process", e);
					}
				}
			}).setNegativeButton("Abbrechen", null).create().show();
			return true;

		case R.id.menuEntryProcessSceneries:
			SceneriesWrapper sceneries = new SceneriesWrapper();
			sceneries.sceneries = roomService.getRoomConfiguration(selectedServer).getSceneries();

			EditConfigHandlerFragment fragEditSceneries = new EditConfigHandlerFragment();
			fragEditSceneries.setTargetFragment(this, EditConfigHandlerFragment.REQ_RETURN_OBJECT);
			Bundle argumentsSceneries = new Bundle();
			argumentsSceneries.putSerializable(EditConfigHandlerFragment.OBJECT_VALUE, sceneries);
			argumentsSceneries.putBoolean(EditConfigHandlerFragment.CREATE_MODE, false);
			argumentsSceneries.putString(EditConfigHandlerFragment.SELECTED_SERVER, selectedServer);
			fragEditSceneries.setArguments(argumentsSceneries);
			FragmentTransaction ft3 = getFragmentManager().beginTransaction();
			ft3.replace(R.id.LayoutMain, fragEditSceneries);
			ft3.addToBackStack(null);
			ft3.commit();

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
					EditConfigHandlerFragment.editConfigBean(ProcessCardFragment.this, drawer.getSelectedNode(), selectedServer,
							roomService.getRoomConfiguration(selectedServer));
					// EditConfigHandlerFragment fragEdit = new
					// EditConfigHandlerFragment();
					// fragEdit.setTargetFragment(myself,
					// EditConfigHandlerFragment.REQ_RETURN_OBJECT);
					// Bundle arguments = new Bundle();
					// arguments.putSerializable(EditConfigHandlerFragment.OBJECT_VALUE,
					// drawer.getSelectedNode());
					// arguments.putBoolean(EditConfigHandlerFragment.CREATE_MODE,
					// false);
					// arguments.putString(EditConfigHandlerFragment.SELECTED_SERVER,
					// selectedServer);
					// fragEdit.setArguments(arguments);
					// FragmentTransaction ft2 =
					// getFragmentManager().beginTransaction();
					// ft2.replace(R.id.LayoutMain, fragEdit);
					// ft2.addToBackStack(null);
					// ft2.commit();
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

			ValidationResult result = RestClient.addProcess(selectedServer, selectedProcess);

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


	private String initRoomArrays(String selectedServer) {
		roomNames.clear();
		serverNames.clear();
		for (String serverName : roomService.getAllRoomConfigurationsMap().keySet()) {
			serverNames.add(serverName);
			roomNames.add(roomService.getAllRoomConfigurationsMap().get(serverName).roomName);
		}

		if (selectedServer == null) {
			selectedServer = serverNames.get(0);
		}

		processNames.clear();
		for (ProcessConfiguration config : roomService.getRoomConfiguration(selectedServer).processes) {
			processNames.add(config.id);
		}
		if (roomAdapter != null) {
			roomAdapter.notifyDataSetChanged();
		}
		if (processAdapter != null) {
			processAdapter.notifyDataSetChanged();
		}
		return selectedServer;
	}


	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(BUNDLE_SELECTED_SERVER, selectedServer);
		savedInstanceState.putSerializable(BUNDLE_SELECTED_PROCESS, selectedProcess);
		savedInstanceState.putBoolean(BUNDLE_EDIT_MODE, editMode);
		super.onSaveInstanceState(savedInstanceState);

	}


	/*
	 * callback method is called everytime a configuration is edited.
	 * 
	 * @see org.ambient.control.processes.IntegrateObjectValueHandler#
	 * integrateConfiguration(java.lang.Object)
	 */
	@Override
	public void onIntegrateConfiguration(String serverName, Object configuration) {

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
			SceneryEventGeneratorConfiguration sceneryEventGenerator = roomService.getRoomConfiguration(selectedServer)
					.getSceneryEventGenerator().values().iterator().next();

			sceneryEventGenerator.sceneries = ((SceneriesWrapper) configuration).sceneries;
			try {
				RestClient.createOrUpdateEventGeneratorConfiguration(selectedServer, sceneryEventGenerator);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/*
	 * do not forget. if a different fragments thread calls this method the
	 * spinners will not be updated. therefore the boolean flag will signal
	 * onResumeWith...() to hide the spinners for itself in the right thread
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
	 * @see
	 * org.ambient.control.config.EditConfigExitListener#onRevertConfiguration
	 * (java.lang.String, java.lang.Object)
	 */
	@Override
	public void onRevertConfiguration(String serverName, Object configuration) {

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambient.control.RoomServiceAwareFragment#onRoomConfigurationChange
	 * (java.lang.String, org.ambientlight.room.RoomConfiguration)
	 */
	@Override
	public void onRoomConfigurationChange(String serverName, RoomConfiguration roomConfiguration) {
		if (serverName.equals(selectedServer)) {
			initRoomArrays(selectedServer);
			if (editMode == false) {

				spinnerProcess.setSelection(getPositionOfSelectedProcess());
				// for the case that the spinner has not changed its position we
				// will restore the process here
				selectedProcess = roomConfiguration.processes.get(getPositionOfSelectedProcess());
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
