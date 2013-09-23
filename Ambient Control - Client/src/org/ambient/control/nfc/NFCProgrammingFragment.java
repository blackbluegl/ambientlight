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

package org.ambient.control.nfc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.ambient.control.IRoomServiceCallbackListener;
import org.ambient.control.MainActivity;
import org.ambient.control.R;
import org.ambient.roomservice.RoomConfigService;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.eventgenerator.SwitchEventGeneratorConfiguration;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


/**
 * @author Florian Bornkessel
 * 
 */
public class NFCProgrammingFragment extends Fragment implements IRoomServiceCallbackListener {

	private RoomConfigService roomService = null;

	private View content;
	private final List<String> serverNames = new ArrayList<String>();
	private final List<String> roomNames = new ArrayList<String>();

	NfcAdapter adapter;
	PendingIntent pendingIntent;
	IntentFilter writeTagFilters[];
	public Tag mytag = null;
	boolean writeMode;
	String selectedServer = null;
	String selectedItem = null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		content = inflater.inflate(R.layout.fragment_nfc_programming, null);

		adapter = NfcAdapter.getDefaultAdapter(this.getActivity());

		pendingIntent = PendingIntent.getActivity(this.getActivity(), 0, new Intent(this.getActivity(), this.getActivity()
				.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		tagDetected.addCategory(Intent.CATEGORY_DEFAULT);

		IntentFilter tagNDEFDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		tagNDEFDetected.addCategory(Intent.CATEGORY_DEFAULT);
		try {
			tagNDEFDetected.addDataType("application/org.ambientcontrol");
		} catch (MalformedMimeTypeException e) {
			e.printStackTrace();
		}

		writeTagFilters = new IntentFilter[] { tagDetected, tagNDEFDetected };

		this.initRoomArrays();

		ArrayAdapter<String> roomAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_dropdown_item_1line, roomNames);

		Spinner spinnerRoom = (Spinner) content.findViewById(R.id.spinnerRoom);

		final Spinner spinnerSwitch = (Spinner) content.findViewById(R.id.spinnerSwitch);

		spinnerRoom.setAdapter(roomAdapter);

		spinnerRoom.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// String selectedRoom = (String) parent.getItemAtPosition(pos);
				selectedServer = serverNames.get(pos);
				RoomConfiguration roomConfig = roomService.getRoomConfiguration(
						selectedServer);

				List<String> switchNames = new ArrayList<String>();
				for (SwitchEventGeneratorConfiguration currentSwitch : roomConfig.getSwitchGenerators().values()) {
					switchNames.add(currentSwitch.name);
				}

				ArrayAdapter<String> switchesAdapter = new ArrayAdapter<String>(getActivity(),
						android.R.layout.simple_dropdown_item_1line, switchNames);
				spinnerSwitch.setAdapter(switchesAdapter);
			}


			@Override
			public void onNothingSelected(AdapterView<?> paramAdapterView) {

			}
		});

		spinnerSwitch.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
				selectedItem = (String) parent.getItemAtPosition(pos);
			}


			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		Button button = (Button) content.findViewById(R.id.buttonProgramNFC);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					String message = selectedServer + NFCSwitchHandlerActivity.MESSAGE_DELIMITER
							+ NFCSwitchHandlerActivity.TYPE_SWITCH_EVENT + NFCSwitchHandlerActivity.MESSAGE_DELIMITER
							+ selectedItem;

					if (adapter == null) {
						Toast.makeText(getActivity(), "Dieses Gerät unterstützt NFC leider nicht!", Toast.LENGTH_LONG).show();
						return;
					}

					if (mytag == null) {
						Toast.makeText(getActivity(), "Kein Tag gefunden", Toast.LENGTH_LONG).show();
					} else {
						write(message, mytag);
						Toast.makeText(getActivity(), "Tag angelernt", Toast.LENGTH_LONG).show();
					}
				} catch (IOException e) {
					Toast.makeText(getActivity(), "Fehler beim Schreiben auf Tag", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				} catch (FormatException e) {
					Toast.makeText(getActivity(), "Fehler beim Schreiben auf Tag: ungülltiges Format", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}
		});

		return content;
	}


	private void initRoomArrays() {
		for (String serverName : ((MainActivity) getActivity()).getAllRoomServers()) {
			serverNames.add(serverName);
			roomNames.add(roomService.getRoomConfiguration(serverName).roomName);
		}
	}


	@Override
	public void onPause() {
		super.onPause();
		WriteModeOff();
	}


	@Override
	public void onResume() {
		super.onResume();
		WriteModeOn();
	}


	private void WriteModeOn() {
		writeMode = true;
		if (adapter != null) {
			adapter.enableForegroundDispatch(this.getActivity(), pendingIntent, writeTagFilters, null);
		}
	}


	private void WriteModeOff() {
		writeMode = false;
		if (adapter != null) {
			adapter.disableForegroundDispatch(this.getActivity());
		}
	}


	@SuppressLint("NewApi")
	private void write(String text, Tag tag) throws IOException, FormatException {

		NdefRecord[] records = { NdefRecord.createMime("application/org.ambientcontrol", text.getBytes(Charset.forName("UTF-8"))) };
		NdefMessage message = new NdefMessage(records);
		// Get an instance of Ndef for the tag.
		Ndef ndef = Ndef.get(tag);
		// Enable I/O
		ndef.connect();
		// Write the message
		ndef.writeNdefMessage(message);
		// Close the connection
		ndef.close();
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
		// TODO Auto-generated method stub
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
