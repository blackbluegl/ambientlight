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

		Map<String, String> vars = Collections.singletonMap("room", params[0]);

		try {
			return Rest.getRestTemplate().getForObject(Rest.getUrl(URL), Room.class, vars);
		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
			return null;
		}
	}
}