package org.ambient.control.nfc;

import org.ambient.control.RoomConfigManager;
import org.ambient.control.rest.RestClient;
import org.ambientlight.process.events.SwitchEventConfiguration;
import org.ambientlight.room.IUserRoomItem;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.eventgenerator.SwitchEventGeneratorConfiguration;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class NFCSwitchHandlerActivity extends Activity {

	final static String TYPE_SWITCH_EVENT = "switchEvent";
	final static String TYPE_ROOM_ITEM = "roomItem";
	final static String MESSAGE_DELIMITER = "@@:@@";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handleNFCIntent(getIntent());
		finish();
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		handleNFCIntent(intent);
		finish();

	}


	/**
	 * @param intent
	 */
	public void handleNFCIntent(Intent intent) {

		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

			Tag mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Ndef ndefTag = Ndef.get(mytag);
			NdefMessage message = ndefTag.getCachedNdefMessage();
			for (NdefRecord record : message.getRecords()) {
				byte[] recordValue = record.getPayload();
				if (recordValue.length > 0) {
					String value = new String(recordValue);
					Log.v("NFCSwitchHandler", "NFC Tag handled: " + value);
					this.createEvent(value);
				}
			}
		}
	}


	/**
	 * @param value
	 */
	private void createEvent(String value) {
		String[] values = value.split(MESSAGE_DELIMITER);
		String server = values[0];
		String type = values[1];
		String itemName = values[2];
		boolean powerState = true;

		try {
			RoomConfigManager adapter = new RoomConfigManager();
			RestClient rest = new RestClient(adapter);
			RoomConfiguration config = RestClient.getRoom(server);

			if (type.equals(TYPE_ROOM_ITEM)) {
				IUserRoomItem roomItem = config.getUserRoomItems().get(itemName);
				powerState = !roomItem.getPowerState();
				rest.setPowerStateForRoomItem(server, itemName, powerState);
			}
			if (type.equals(TYPE_SWITCH_EVENT)) {
				SwitchEventGeneratorConfiguration switchConfig = config.getSwitchGenerators().get(itemName);
				SwitchEventConfiguration event = new SwitchEventConfiguration();
				event.eventGeneratorName = itemName;
				powerState = !switchConfig.getPowerState();
				event.powerState = powerState;
				rest.sendEvent(server, event);
			}

			if (powerState) {
				Toast.makeText(this, "Schalte " + itemName + " ein", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Schalte " + itemName + " aus", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
