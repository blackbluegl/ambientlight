package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import org.ambient.rest.callbacks.GetRoomResulthandler;
import org.ambientlight.ws.Room;

import android.os.AsyncTask;
import android.util.Log;


public class GetRoomTask extends AsyncTask<Object, Void, Room> {

	private static final String LOG = "GetRoomTask";

	private final String URL = "/rooms/config/{room}";

	private GetRoomResulthandler resultHandler = null;
	private String roomName = null;


	@Override
	protected Room doInBackground(Object... params) {

		this.roomName = (String) params[0];
		this.resultHandler = (GetRoomResulthandler) params[1];

		Map<String, String> vars = Collections.singletonMap("room", roomName);

		try {
			Log.d(LOG, "get roomconfig for: " + roomName);
			return Rest.getRestTemplate().getForObject(Rest.getUrl(URL), Room.class, vars);
		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
			return null;
		}
	}


	@Override
	protected void onPostExecute(Room result) {
		resultHandler.onGetRoomResult(roomName, result);
	}
}