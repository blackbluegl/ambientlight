package org.ambient.rest;

import java.util.List;

import android.os.AsyncTask;
import android.util.Log;


public class GetRoomNamesTask extends AsyncTask<String, Void, List<String>> {

	private static final String LOG = GetRoomNamesTask.class.getName();

	private final String URL = "/rooms/names";


	@SuppressWarnings("unchecked")
	@Override
	protected List<String> doInBackground(String... params) {
		try {
			return Rest.getRestTemplate().getForObject(Rest.getUrl(URL), List.class);
		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
			return null;
		}
	}
}