package org.ambient.rest;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;


public class DeleteProcessTask extends AsyncTask<String, Void, Void> {

	private static final String LOG = DeleteProcessTask.class.getName();

	private final String URL = "/process/{roomName}/{id}";


	@Override
	protected Void doInBackground(String... params) {

		try {
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("roomName", params[0]);
			vars.put("id", params[1]);

			Rest.getRestTemplate().delete(Rest.getUrl(URL), vars);
		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
		}

		return null;
	}
}