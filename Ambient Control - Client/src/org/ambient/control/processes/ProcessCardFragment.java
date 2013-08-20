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

import org.ambient.control.MainActivity;
import org.ambient.control.R;
import org.ambient.views.ProcessCardDrawer;
import org.ambient.views.ProcessCardDrawer.NodeSelectionListener;
import org.ambientlight.process.NodeConfiguration;
import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.room.RoomConfiguration;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class ProcessCardFragment extends Fragment implements IntegrateObjectValueHandler {

	// private NodeConfiguration selectedNode = null;

	private final String BUNDLE_SPINNER_ROOM_POSITION = "bundleSpinnerRoomPosition";

	// helper list and values to correlate between the two spinners
	private final List<String> serverNames = new ArrayList<String>();
	String selectedServer = null;
	// String selectedProcessPosition = null;
	ProcessConfiguration selectedProcess = null;

	// adapter values of the two spinners
	private final List<String> roomNames = new ArrayList<String>();
	private final List<String> processNames = new ArrayList<String>();

	View content;

	Spinner spinnerRoom;
	Spinner spinnerProcess;
	ProcessCardDrawer drawer;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
		// getActivity().getActionBar().setDisplayShowHomeEnabled(false);
		this.content = inflater.inflate(R.layout.fragment_processcard, container, false);
		drawer = (ProcessCardDrawer) content.findViewById(R.id.processCardDrawer);
		if (savedInstanceState != null) {
			int position = savedInstanceState.getInt(BUNDLE_SPINNER_ROOM_POSITION);
			this.initRoomArrays(position);
		} else {
			this.initRoomArrays(0);
		}

		final ArrayAdapter<String> roomAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_spinner_item, roomNames);
		roomAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

		final ArrayAdapter<String> switchesAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_spinner_item, processNames);
		switchesAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

		spinnerRoom = (Spinner) content.findViewById(R.id.spinnerProcessRoom);
		spinnerProcess = (Spinner) content.findViewById(R.id.spinnerProcess);

		spinnerProcess.setAdapter(switchesAdapter);
		spinnerRoom.setAdapter(roomAdapter);

		// create the listener after the initialisation. we do not want to
		// affect the second spinner while its in creation. let android this for
		// us (onResume, Screenrotation etc.)
		spinnerRoom.post(new Runnable() {

			@Override
			public void run() {
				spinnerRoom.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

						getActivity().invalidateOptionsMenu();

						selectedServer = serverNames.get(pos);
						RoomConfiguration selectedRoomConfiguration = ((MainActivity) getActivity()).getRoomConfigManager()
								.getRoomConfiguration(selectedServer);

						processNames.clear();
						for (ProcessConfiguration currentProcess : selectedRoomConfiguration.processes) {
							processNames.add(currentProcess.id);
						}

						switchesAdapter.notifyDataSetChanged();
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
				RoomConfiguration selectedRoomConfiguration = ((MainActivity) getActivity()).getRoomConfigManager()
						.getRoomConfiguration(selectedServer);
				selectedProcess = selectedRoomConfiguration.processes.get(pos);
				drawer.setProcess(selectedProcess);
			}


			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		final ProcessCardFragment myself = this;
		drawer.setOnNodeSelectionListener(new NodeSelectionListener() {

			@Override
			public void onNodeSelected(NodeConfiguration node) {
				// getActivity().invalidateOptionsMenu();
				myself.getActivity().startActionMode(new ActionMode.Callback() {

					@Override
					public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
						mode.finish();
						switch (item.getItemId()) {

						case R.id.menuEntryProcessRemoveNode:
							if (drawer.getSelectedNode().nextNodeIds.size() == 1) {
								for (NodeConfiguration currentPreviousNode : selectedProcess.nodes.values()) {
									if (currentPreviousNode.nextNodeIds != null && currentPreviousNode.nextNodeIds.size() > 0
											&& currentPreviousNode.nextNodeIds.get(0).equals(drawer.getSelectedNode().id)) {
										currentPreviousNode.nextNodeIds = drawer.getSelectedNode().nextNodeIds;
										selectedProcess.nodes.remove(drawer.getSelectedNode().id);
										drawer.setProcess(selectedProcess);
										break;
									}
								}
							}
							return true;
						case R.id.menuEntryProcessAddSecondNode:
							NodeConfiguration secondNodeConfig = new NodeConfiguration();

							for (Integer i = 0; i <= selectedProcess.nodes.keySet().size(); i++) {
								if (selectedProcess.nodes.containsKey(i) == false) {
									secondNodeConfig.id = i;
									myself.drawer.getSelectedNode().nextNodeIds.add(secondNodeConfig.id);
									selectedProcess.nodes.put(i, secondNodeConfig);
									drawer.setProcess(selectedProcess);
									drawer.setSelectdeNode(secondNodeConfig);
									break;
								}
							}
							return true;

						case R.id.menuEntryProcessAddNode:
							if (myself.drawer.getSelectedNode().nextNodeIds.size() > 1) {
								// create dialog
							} else {
								NodeConfiguration nodeConfig = new NodeConfiguration();
								if (myself.drawer.getSelectedNode().nextNodeIds.isEmpty() == false) {
									nodeConfig.nextNodeIds.add(myself.drawer.getSelectedNode().nextNodeIds.get(0));
								}

								for (Integer i = 0; i <= selectedProcess.nodes.keySet().size(); i++) {
									if (selectedProcess.nodes.containsKey(i) == false) {
										nodeConfig.id = i;
										myself.drawer.getSelectedNode().nextNodeIds.clear();
										myself.drawer.getSelectedNode().nextNodeIds.add(nodeConfig.id);
										selectedProcess.nodes.put(i, nodeConfig);
										drawer.setProcess(selectedProcess);
										drawer.setSelectdeNode(nodeConfig);
										break;
									}
								}
							}
							return true;

						case R.id.menuEntryProcessEditNode:
							EditConfigHandlerFragment fragEdit = new EditConfigHandlerFragment();
							fragEdit.setTargetFragment(myself, EditConfigHandlerFragment.REQ_RETURN_OBJECT);
							Bundle arguments = new Bundle();
							arguments.putSerializable(EditConfigHandlerFragment.OBJECT_VALUE, myself.drawer.getSelectedNode());
							arguments.putBoolean(EditConfigHandlerFragment.CREATE_MODE, false);
							arguments.putString(EditConfigHandlerFragment.SELECTED_SERVER, selectedServer);
							fragEdit.setArguments(arguments);
							FragmentTransaction ft2 = getFragmentManager().beginTransaction();
							ft2.replace(R.id.LayoutMain, fragEdit);
							ft2.addToBackStack(null);
							ft2.commit();
							return true;
						default:
							return true;

						}
					}


					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {
						mode.getMenuInflater().inflate(R.menu.fragment_processcard_cab, menu);
						// inflater.inflate(R.menu.fragment_processcard_menu,
						// menu);
						MenuItem edit = menu.findItem(R.id.menuEntryProcessEditNode);
						MenuItem add = menu.findItem(R.id.menuEntryProcessAddNode);
						MenuItem addSecond = menu.findItem(R.id.menuEntryProcessAddSecondNode);
						MenuItem remove = menu.findItem(R.id.menuEntryProcessRemoveNode);
						if (myself.drawer.getSelectedNode() != null) {
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
						// TODO Auto-generated method stub
						myself.drawer.setSelectdeNode(null);

					}


					@Override
					public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
						// TODO Auto-generated method stub
						return false;
					}
				});

			}
		});
		setHasOptionsMenu(true);
		return content;
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_processcard_menu, menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		default:
			return super.onOptionsItemSelected(item);
		}
	}


	private void initRoomArrays(int roomSpinnerSelection) {
		for (String serverName : ((MainActivity) getActivity()).getRoomConfigManager().getAllRoomConfigurations().keySet()) {
			serverNames.add(serverName);
			roomNames
			.add(((MainActivity) getActivity()).getRoomConfigManager().getAllRoomConfigurations().get(serverName).roomName);
		}

		this.selectedServer = serverNames.get(roomSpinnerSelection);

		for (ProcessConfiguration config : ((MainActivity) getActivity()).getRoomConfigManager().getAllRoomConfigurations()
				.get(serverNames.get(roomSpinnerSelection)).processes) {
			processNames.add(config.id);
		}
	}


	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt(BUNDLE_SPINNER_ROOM_POSITION, spinnerRoom.getSelectedItemPosition());
		super.onSaveInstanceState(savedInstanceState);

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.processes.IntegrateObjectValueHandler#
	 * integrateConfiguration(java.lang.Object)
	 */
	@Override
	public void integrateConfiguration(Object configuration) {
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
		getActivity().getActionBar().setDisplayShowHomeEnabled(false);
		Log.i("ProcessCardFragment", "object returned");

	}
}
