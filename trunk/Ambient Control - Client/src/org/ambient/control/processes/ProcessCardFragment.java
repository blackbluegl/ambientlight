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
import android.util.Log;
import android.view.LayoutInflater;
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
public class ProcessCardFragment extends Fragment {

	private final String BUNDLE_SPINNER_ROOM_POSITION = "bundleSpinnerRoomPosition";

	// helper list and values to correlate between the two spinners
	private final List<String> serverNames = new ArrayList<String>();
	String selectedServer = null;
	String selectedProcessPosition = null;

	// adapter values of the two spinners
	private final List<String> roomNames = new ArrayList<String>();
	private final List<String> processNames = new ArrayList<String>();


	View content;

	Spinner spinnerRoom;
	Spinner spinnerProcess;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.content = inflater.inflate(R.layout.fragment_processcard, container, false);
		final ProcessCardDrawer drawer = (ProcessCardDrawer) content.findViewById(R.id.processCardDrawer);

		if (savedInstanceState != null) {
			int position = savedInstanceState.getInt(BUNDLE_SPINNER_ROOM_POSITION);
			this.initRoomArrays(position);
		} else {
			this.initRoomArrays(0);
		}

		final ArrayAdapter<String> roomAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_spinner_item, roomNames);
		final ArrayAdapter<String> switchesAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_spinner_item, processNames);

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

				RoomConfiguration selectedRoomConfiguration = ((MainActivity) getActivity()).getRoomConfigManager()
						.getRoomConfiguration(selectedServer);

				drawer.setProcess(selectedRoomConfiguration.processes.get(pos));

			}


			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		drawer.setOnNodeSelectionListener(new NodeSelectionListener() {

			@Override
			public void onNodeSelected(NodeConfiguration node) {
				Log.i("nodeSelectListener", node.actionHandler.getClass().getSimpleName());
			}
		});
		return content;
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
}
