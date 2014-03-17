package org.ambient.rest;

import java.util.List;

import android.os.AsyncTask;
import android.util.Log;


public class GetRoomNamesTask extends AsyncTask<String, Void, List<String>> {

	private static final String LOG = "GetRoomNamesTask";

	private final String URL = "/rooms/names";


	@Override
	protected List<String> doInBackground(String... params) {
		Log.d(LOG, "is called");

		String url = Rest.getBaseUrl(params[0]) + URL;

		try {
			return Rest.getRestTemplate().getForObject(url, List.class, "");
		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
			return null;
		}
	}
}