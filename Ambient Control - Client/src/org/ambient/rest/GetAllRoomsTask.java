package org.ambient.rest;

import java.util.List;

import org.ambient.rest.callbacks.GetRoomResulthandler;
import org.ambientlight.ws.Room;
import org.ambientlight.ws.RoomsResponse;

import android.os.AsyncTask;
import android.util.Log;


public class GetAllRoomsTask extends AsyncTask<Object, Void, List<Room>> {

	private static final String LOG = GetAllRoomsTask.class.getName();

	private final String URL = "/rooms/configs";

	private GetRoomResulthandler resultHandler = null;


	@Override
	protected List<Room> doInBackground(Object... params) {

		this.resultHandler = (GetRoomResulthandler) params[0];

		try {
			Log.d(LOG, "get all roomconfigs");
			RoomsResponse response = Rest.getRestTemplate().getForObject(Rest.getUrl(URL), RoomsResponse.class);
			return response.rooms;
		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
			return null;
		}
	}


	@Override
	protected void onPostExecute(List<Room> result) {
		resultHandler.onGetRoomResult(result);
	}
}