package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import org.ambientlight.ws.Room;

import android.os.AsyncTask;
import android.util.Log;


public class GetRoomTask extends AsyncTask<String, Void, Room> {

	private static final String LOG = "GetRoomTask";

	private final String URL = "/rooms/config/{room}";


	@Override
	protected Room doInBackground(String... params) {
		Log.d(LOG, "is called");

		String url = Rest.getBaseUrl(params[0]) + URL;
		Map<String, String> vars = Collections.singletonMap("room", params[1]);

		try {
			return Rest.getRestTemplate().getForObject(url, Room.class, vars);
		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
			return null;
		}
	}
}