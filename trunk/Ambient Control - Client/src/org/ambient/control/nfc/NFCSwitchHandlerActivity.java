package org.ambient.control.nfc;

import java.util.HashMap;
import java.util.Locale;

import org.ambient.control.rest.RestClient;
import org.ambientlight.process.events.SwitchEventConfiguration;
import org.ambientlight.room.IUserRoomItem;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.eventgenerator.SceneryEventGeneratorConfiguration;
import org.ambientlight.room.eventgenerator.SwitchEventGeneratorConfiguration;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;


public class NFCSwitchHandlerActivity extends Activity implements OnInitListener {

	final static String TYPE_SWITCH_EVENT = "switchEvent";
	final static String TYPE_ROOM_ITEM = "roomItem";
	final static String MESSAGE_DELIMITER = "@@:@@";

	private TextToSpeech tts;
	private String speakout = "";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handleNFCIntent(getIntent());
		initTTS();
	}


	private void initTTS() {
		tts = new TextToSpeech(this, this);
		tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

			@Override
			public void onStart(String paramString) {
			}


			@Override
			public void onError(String paramString) {
			}


			@Override
			public void onDone(String paramString) {
				if ("EOM".equals(paramString)) {
					finish();
				}
			}
		});
	}


	@Override
	// do nothing here so if a tag will be discovered a second time while
	// the activity is running no unwished action will be performed
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

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
			RestClient rest = new RestClient();
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

			SceneryEventGeneratorConfiguration sceneryEvent = config.getSceneryEventGenerator().get("RoomSceneryEventGenerator");

			if (powerState) {
				speakout = sceneryEvent.currentScenery.id + ", ein";
				Toast.makeText(this, "Schalte " + itemName + " ein", Toast.LENGTH_LONG).show();
			} else {
				speakout = sceneryEvent.currentScenery.id + ", aus";
				Toast.makeText(this, "Schalte " + itemName + " aus", Toast.LENGTH_LONG).show();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {

			int result = tts.setLanguage(Locale.GERMAN);

			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Toast.makeText(this, "Sprachsynthese in dieser Sprache nicht unterstützt", Toast.LENGTH_SHORT).show();
			} else {
				speakOut();
			}
		} else {
			Toast.makeText(this, "Sprachsynthese wurde nicht korrekt gestartet", Toast.LENGTH_SHORT).show();
		}
	}


	private void speakOut() {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "EOM");
		tts.speak(this.speakout, TextToSpeech.QUEUE_FLUSH, params);
	}


	@Override
	public void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}
}
