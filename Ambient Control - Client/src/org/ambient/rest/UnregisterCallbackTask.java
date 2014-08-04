package org.ambient.rest;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;


public class UnregisterCallbackTask extends AsyncTask<String, Void, Void> {

	private static final String LOG = UnregisterCallbackTask.class.getName();

	private final String URL = "/callback/{room}/client/{clientId}";


	@Override
	protected Void doInBackground(String... params) {

		Map<String, String> vars = new HashMap<String, String>();
		vars.put("room", params[0]);
		vars.put("clientId", params[1]);

		try {
			Rest.getRestTemplate().delete(Rest.getUrl(URL), vars);
		} catch (Exception e) {
			// if the server is unreachable just do nothing.
			Log.e(LOG, "caught exception and ignoring it.", e);
		}

		return null;
	}

}