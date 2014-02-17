package org.ambient.control.nfc;

import java.util.HashMap;
import java.util.Locale;

import org.ambient.roomservice.RoomConfigService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;


public class NFCSwitchHandlerActivity extends Activity implements OnInitListener {

	private static final String LOG = "NFCSwitchHandlerActivity";

	final static String TYPE_SWITCH_EVENT = "switchEvent";
	final static String TYPE_ROOM_ITEM = "roomItem";
	final static String MESSAGE_DELIMITER = "@@:@@";

	private TextToSpeech tts = null;
	private boolean ttsReady = false;

	final static String BUNDLE_LAST_CALL = "lastCall";
	private long lastCall = 0;

	private RoomConfigService roomService = null;

	String nfcValue = null;

	// callback handle for roomService connection
	private ServiceConnection roomServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			roomService = ((RoomConfigService.MyBinder) binder).getService();
			createEvent();
		}


		@Override
		public void onServiceDisconnected(ComponentName className) {
			roomService = null;
			NFCSwitchHandlerActivity.this.finish();
		}
	};


	// callback handle for textToSpeach
	@Override
	public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {

			ttsReady = true;

			int result = tts.setLanguage(Locale.GERMAN);

			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Toast.makeText(this, "Sprachsynthese in dieser Sprache nicht unterstützt", Toast.LENGTH_SHORT).show();
			}

			createEvent();
		} else {
			Toast.makeText(this, "Sprachsynthese wurde nicht korrekt gestartet", Toast.LENGTH_SHORT).show();
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			this.lastCall = savedInstanceState.getLong(BUNDLE_LAST_CALL);
		}

		// we have to wait for services to be bound. in callback methods we send
		// the events
		bindService(new Intent(this, RoomConfigService.class), roomServiceConnection, Context.BIND_AUTO_CREATE);

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

				// after sentence was spoken send activity to background
				if ("EOM".equals(paramString)) {
					moveTaskToBack(true);
				}
			}
		});

		// extract value from nfc-tag
		handleNFCIntent(getIntent());
		// we have to wait that all services are set up and ready. so we do not
		// call createEvent() here. It will be called in the service callbacks.
		// or if activity is ready in onNewIntent()
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// extract nfc value
		handleNFCIntent(getIntent());
		createEvent();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(BUNDLE_LAST_CALL, this.lastCall);
	}


	private void handleNFCIntent(Intent intent) {

		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

			Tag mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Ndef ndefTag = Ndef.get(mytag);
			NdefMessage message = ndefTag.getCachedNdefMessage();
			for (NdefRecord record : message.getRecords()) {
				byte[] recordValue = record.getPayload();
				if (recordValue.length > 0) {
					nfcValue = new String(recordValue);
					Log.v("NFCSwitchHandler", "NFC Tag handled: " + nfcValue);
				}
			}
		}
	}


	private void createEvent() {

		// we will be called several times from different callback handlers.
		// thats ok as long as we wait for the call where all services are
		// set and ready.
		if (ttsReady == false || roomService == null)
			return;

		// get shure that we will not be called twice in a short time
		long now = System.currentTimeMillis();
		if (now - lastCall < 2000) {
			Toast.makeText(this, "Doppelter Aufruf nach " + (now - lastCall) + "ms unterbunden", Toast.LENGTH_SHORT).show();
			moveTaskToBack(true);
			return;
		} else {
			lastCall = now;
		}

		String[] values = nfcValue.split(MESSAGE_DELIMITER);
		String server = values[0];
		String type = values[1];
		String itemName = values[2];
		boolean powerState = true;

		try {

			// Room config = roomService.getRoomConfiguration(server);
			//
			// if (type.equals(TYPE_ROOM_ITEM)) {
			// Switchable roomItem = config.getSwitchableActors().get(itemName);
			// powerState = !roomItem.getPowerState();
			// RestClient.setPowerStateForRoomItem(server, itemName,
			// powerState);
			// }
			//
			// if (type.equals(TYPE_SWITCH_EVENT)) {
			// SwitchManagerConfiguration switchConfig =
			// config.getSwitches().get(itemName);
			// SwitchEvent event = new SwitchEvent();
			// event.sourceId = itemName;
			// powerState = !switchConfig.getPowerState();
			// event.powerState = powerState;
			// RestClient.sendEvent(server, event);
			//
			// SceneryManagerConfiguration sceneryEvent =
			// config.getSceneryEventGeneratorConfiguration();
			//
			// if (powerState) {
			// speakOut(sceneryEvent.currentScenery.id + ", ein");
			// Toast.makeText(this, "Schalte " + itemName + " ein - Szenario: "
			// + sceneryEvent.currentScenery.id,
			// Toast.LENGTH_LONG).show();
			// } else {
			// speakOut(sceneryEvent.currentScenery.id + ", aus");
			// Toast.makeText(this, "Schalte " + itemName + " aus",
			// Toast.LENGTH_LONG).show();
			// }
			// }

		} catch (Exception e) {
			Log.e(LOG, "exception raised during creation of event: ", e);
		}

	}


	private void speakOut(String speakOut) {

		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = new long[] { 0, 100, 66, 100 };
		v.vibrate(pattern, -1);

		HashMap<String, String> params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "EOM");
		tts.speak(speakOut, TextToSpeech.QUEUE_FLUSH, params);
	}


	@Override
	public void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		unbindService(roomServiceConnection);
		super.onDestroy();
	}
}
