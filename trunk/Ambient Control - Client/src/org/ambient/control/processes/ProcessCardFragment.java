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

import org.ambient.control.IRoomServiceCallbackListener;
import org.ambient.control.R;
import org.ambient.control.config.EditConfigExitListener;
import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambient.control.processes.helper.SceneriesWrapper;
import org.ambient.control.rest.RestClient;
import org.ambient.roomservice.RoomConfigService;
import org.ambient.views.ProcessCardDrawer;
import org.ambient.views.ProcessCardDrawer.NodeSelectionListener;
import org.ambientlight.process.NodeConfiguration;
import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.process.validation.ValidationEntry;
import org.ambientlight.process.validation.ValidationResult;
import org.ambientlight.room.RoomConfiguration;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
public class ProcessCardFragment extends Fragment implements EditConfigExitListener, IRoomServiceCallbackListener {

	private RoomConfigService roomService = null;

	private static final String BUNDLE_SELECTED_PROCESS = "bundleSelectedProcess";
	private static final String BUNDLE_SPINNER_ROOM_POSITION = "bundleSpinnerRoomPosition";
	private static final String BUNDLE_EDIT_MODE = "bundleEditMode";

	private ActionMode mode = null;

	// these values have to be persisted and restored by myself
	int positionServer = 0;
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
	ProcessCardDrawer drawer;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
		setHasOptionsMenu(true);

		this.content = inflater.inflate(R.layout.fragment_processcard, container, false);

		drawer = (ProcessCardDrawer) content.findViewById(R.id.processCardDrawer);

		if (savedInstanceState != null) {
			positionServer = savedInstanceState.getInt(BUNDLE_SPINNER_ROOM_POSITION);
			selectedProcess = (ProcessConfiguration) savedInstanceState.getSerializable(BUNDLE_SELECTED_PROCESS);
			editMode = savedInstanceState.getBoolean(BUNDLE_EDIT_MODE);
		}

		initRoomArrays(positionServer);

		final ArrayAdapter<String> roomAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_spinner_item, roomNames);
		roomAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

		processAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, processNames);
		processAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

		spinnerRoom = (Spinner) content.findViewById(R.id.spinnerProcessRoom);
		spinnerProcess = (Spinner) content.findViewById(R.id.spinnerProcess);
		spinnerRoom.setAdapter(roomAdapter);
		spinnerProcess.setAdapter(processAdapter);

		if (editMode == true) {
			spinnerRoom.setVisibility(View.GONE);
			spinnerProcess.setVisibility(View.GONE);
			drawer.setProcess(selectedProcess);
			getActivity().invalidateOptionsMenu();
		}

		// create the listener after the initialisation. we do not want to
		// affect the second spinner while its in creation. let android this
		// for us (onResume, Screenrotation etc.)
		spinnerRoom.post(new Runnable() {

			@Override
			public void run() {
				spinnerRoom.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
						editMode = false;
						getActivity().invalidateOptionsMenu();

						selectedServer = serverNames.get(pos);
						positionServer = pos;
						RoomConfiguration selectedRoomConfiguration = roomService
								.getRoomConfiguration(selectedServer);

						processNames.clear();
						for (ProcessConfiguration currentProcess : selectedRoomConfiguration.processes) {
							processNames.add(currentProcess.id);
						}

						processAdapter.notifyDataSetChanged();
						spinnerProcess.setSelection(0);
					}


					@Override
					public void onNothingSelected(AdapterView<?> paramAdapterView) {

					}
				});
			}
		});

		spinnerProcess.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
				getActivity().invalidateOptionsMenu();
				RoomConfiguration selectedRoomConfiguration = roomService
						.getRoomConfiguration(selectedServer);
				selectedProcess = selectedRoomConfiguration.processes.get(pos);
				drawer.setProcess(selectedProcess);
			}


			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
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

		return content;
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_processcard_menu, menu);
		if (editMode) {
			menu.findItem(R.id.menuEntryProcessRevertNew).setVisible(true);
			menu.findItem(R.id.menuEntryProcessRemove).setVisible(false);
			menu.findItem(R.id.menuEntryProcessSave).setVisible(true);
			menu.findItem(R.id.menuEntryProcessStart).setVisible(false);
			menu.findItem(R.id.menuEntryProcessStop).setVisible(false);
			menu.findItem(R.id.menuEntryProcessAdd).setVisible(false);
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		RestClient restClient = new RestClient();
		switch (item.getItemId()) {

		case R.id.menuEntryProcessStart:
			restClient.startProcess(selectedServer, selectedProcess.id);
			return true;

		case R.id.menuEntryProcessStop:
			restClient.stopProcess(selectedServer, selectedProcess.id);
			return true;

		case R.id.menuEntryProcessValidate:
			ValidationResult result = null;
			try {
				result = restClient.validateProcess(selectedServer, selectedProcess);
			} catch (Exception e) {
				e.printStackTrace();
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
			// this.editMode = false;
			// spinnerProcess.setSelection(0);
			// RoomConfigService manager = ((MainActivity)
			// getActivity()).getRoomConfigManager();
			// try {
			// manager.addRoomConfiguration(selectedServer,
			// RestClient.getRoom(selectedServer));
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// selectedProcess =
			// manager.getRoomConfiguration(selectedServer).processes.get(0);
			// drawer.setProcess(selectedProcess);
			// getActivity().invalidateOptionsMenu();
			// spinnerRoom.setVisibility(View.VISIBLE);
			// spinnerProcess.setVisibility(View.VISIBLE);
			return true;

		case R.id.menuEntryProcessSave:
			Boolean saveProcess = true;

			try {
				for (ProcessConfiguration current : restClient.getRoom(selectedServer).processes) {
					if (current.id.equals(selectedProcess.id)) {
						saveProcess = false;
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
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
						//
						// new
						// RestClient(null).deleteProcessFromRoom(selectedServer,
						// selectedProcess.id);
						// RoomConfigManager manager = ((MainActivity)
						// getActivity()).getRoomConfigManager();
						// manager.addRoomConfiguration(selectedServer,
						// RestClient.getRoom(selectedServer));
						// selectedProcess =
						// manager.getRoomConfiguration(selectedServer).processes.get(0);
						// drawer.setProcess(selectedProcess);
						//
						// processNames.clear();
						// for (ProcessConfiguration currentProcess :
						// manager.getRoomConfiguration(selectedServer).processes)
						// {
						// processNames.add(currentProcess.id);
						// }
						// processAdapter.notifyDataSetChanged();
						// spinnerProcess.setSelection(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).setNegativeButton("Abbrechen", null).create().show();
			return true;

		case R.id.menuEntryProcessSceneries:
			SceneriesWrapper sceneries = new SceneriesWrapper();
			sceneries.sceneries = roomService.getRoomConfiguration(selectedServer)
					.getSceneries();

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
	 * 
	 * @return
	 */
	public ActionMode createActionMode() {
		final ProcessCardFragment myself = this;
		return getActivity().startActionMode(new ActionMode.Callback() {

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {

				case R.id.menuEntryProcessRemoveNode:
					// handling for nodes with a previous node in the
					// process
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
					EditConfigHandlerFragment fragEdit = new EditConfigHandlerFragment();
					fragEdit.setTargetFragment(myself, EditConfigHandlerFragment.REQ_RETURN_OBJECT);
					Bundle arguments = new Bundle();
					arguments.putSerializable(EditConfigHandlerFragment.OBJECT_VALUE, drawer.getSelectedNode());
					arguments.putBoolean(EditConfigHandlerFragment.CREATE_MODE, false);
					arguments.putString(EditConfigHandlerFragment.SELECTED_SERVER, selectedServer);
					fragEdit.setArguments(arguments);
					FragmentTransaction ft2 = getFragmentManager().beginTransaction();
					ft2.replace(R.id.LayoutMain, fragEdit);
					ft2.addToBackStack(null);
					ft2.commit();
					break;

				default:
					break;

				}

				mode.finish();
				editMode = true;
				spinnerProcess.setVisibility(View.GONE);
				spinnerRoom.setVisibility(View.GONE);
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
			RestClient rest = new RestClient();
			ValidationResult result = rest.addProcess(selectedServer, selectedProcess);

			if (result.resultIsValid() == false) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Fehler").setMessage("Es wurden Fehler gefunden. Der Prozess wurde nicht gespeichert.")
				.setPositiveButton("OK", null).create().show();
				for (ValidationEntry entry : result.invalidateEntries) {
					drawer.addNodeWithError(selectedProcess.nodes.get(entry.nodeId), entry);
				}
			} else {
				editMode = false;
				getActivity().invalidateOptionsMenu();
				spinnerRoom.setVisibility(View.VISIBLE);
				spinnerProcess.setVisibility(View.VISIBLE);

				// try {
				// RoomConfigManager manager = ((MainActivity)
				// getActivity()).getRoomConfigManager();
				// manager.addRoomConfiguration(selectedServer,
				// RestClient.getRoom(selectedServer));
				// } catch (Exception e) {
				// e.printStackTrace();
				// }

				processNames.clear();
				// for (ProcessConfiguration config : ((MainActivity)
				// getActivity()).getRoomConfigManager()
				// .getAllRoomConfigurations().get(serverNames.get(spinnerRoom.getSelectedItemPosition())).processes)
				// {
				// processNames.add(config.id);
				// }
				processAdapter.notifyDataSetChanged();
				spinnerProcess.setSelection(processNames.indexOf(selectedProcess.id));
				// selectedProcess =
				// manager.getRoomConfiguration(selectedServer).processes.get(0);
				// drawer.setProcess(selectedProcess);
				// getActivity().invalidateOptionsMenu();
				// spinnerRoom.setVisibility(View.VISIBLE);
				// spinnerProcess.setVisibility(View.VISIBLE);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void initRoomArrays(int roomSpinnerSelection) {
		roomNames.clear();
		serverNames.clear();
		// for (String serverName : ((MainActivity)
		// getActivity()).getRoomConfigManager().getAllRoomConfigurations().keySet())
		// {
		// serverNames.add(serverName);
		// roomNames
		// .add(((MainActivity)
		// getActivity()).getRoomConfigManager().getAllRoomConfigurations().get(serverName).roomName);
		// }

		this.selectedServer = serverNames.get(roomSpinnerSelection);

		processNames.clear();
		// for (ProcessConfiguration config : ((MainActivity)
		// getActivity()).getRoomConfigManager().getAllRoomConfigurations()
		// .get(serverNames.get(roomSpinnerSelection)).processes) {
		// processNames.add(config.id);
		// }
	}


	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt(BUNDLE_SPINNER_ROOM_POSITION, positionServer);
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
			this.editMode = true;
			this.selectedProcess = (ProcessConfiguration) configuration;
			if (this.selectedProcess.nodes.isEmpty()) {
				NodeConfiguration node = new NodeConfiguration();
				this.selectedProcess.nodes.put(0, node);
			}
		}
		if (configuration instanceof NodeConfiguration) {
			this.editMode = true;
			NodeConfiguration nodeConfig = (NodeConfiguration) configuration;
			this.selectedProcess.nodes.put(nodeConfig.id, nodeConfig);
		}

		if (configuration instanceof SceneriesWrapper) {
			this.editMode = false;

			// SceneryEventGeneratorConfiguration sceneryEventGenerator =
			// ((MainActivity) getActivity()).getRoomConfigManager()
			// .getAllRoomConfigurations().get(selectedServer).getSceneryEventGenerator().values().iterator().next();

			// sceneryEventGenerator.sceneries = ((SceneriesWrapper)
			// configuration).sceneries;
			RestClient rest = new RestClient();
			// try {
			// rest.createOrUpdateEventGeneratorConfiguration(selectedServer,
			// sceneryEventGenerator);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
		}
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
		// TODO Auto-generated method stub

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambient.control.IRoomServiceCallbackListener#onRoomServiceConnected
	 * (org.ambient.roomservice.RoomConfigService)
	 */
	@Override
	public void onRoomServiceConnected(RoomConfigService service) {
		roomService = service;

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambient.control.IRoomServiceCallbackListener#onRoomConfigurationChange
	 * (java.lang.String, org.ambientlight.room.RoomConfiguration)
	 */
	@Override
	public void onRoomConfigurationChange(String serverName, RoomConfiguration roomConfiguration) {
		// TODO Auto-generated method stub

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambient.control.IRoomServiceCallbackListener#setRoomService(org.ambient
	 * .roomservice.RoomConfigService)
	 */
	@Override
	public void setRoomService(RoomConfigService roomService) {
		this.roomService = roomService;

	}
}
