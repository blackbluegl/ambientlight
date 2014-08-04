package org.ambient.rest;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;


public class CreateSceneryTask extends AsyncTask<String, Void, Void> {

	private static final String LOG = CreateSceneryTask.class.getName();

	private final String URL = "/sceneries/{roomName}/new";


	@Override
	protected Void doInBackground(String... params) {

		try {
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("roomName", params[0]);

			Rest.getRestTemplate().put(Rest.getUrl(URL), params[1], vars);
			return null;
		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
			return null;
		}

	}
}