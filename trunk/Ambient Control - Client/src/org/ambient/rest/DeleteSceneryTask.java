package org.ambient.rest;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;


public class DeleteSceneryTask extends AsyncTask<String, Void, Void> {

	private static final String LOG = DeleteSceneryTask.class.getName();

	private final String URL = "/sceneries/{roomName}/{id}";


	@Override
	protected Void doInBackground(String... params) {

		try {
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("roomName", params[0]);
			vars.put("id", params[1]);

			Rest.getRestTemplate().delete(Rest.getUrl(URL), vars);
		} catch (Exception e) {
			Log.e(LOG, "Caught Exception!", e);
		}

		return null;
	}
}