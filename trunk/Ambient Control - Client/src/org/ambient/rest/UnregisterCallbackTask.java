package org.ambient.rest;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;


public class UnregisterCallbackTask extends AsyncTask<String, Void, Void> {

	private final String URL = "/callback/{room}/client/{clientId}";


	@Override
	protected Void doInBackground(String... params) {

		Map<String, String> vars = new HashMap<String, String>();
		vars.put("room", params[0]);
		vars.put("clientId", params[1]);

		try {
			Rest.getRestTemplate().delete(Rest.getUrl(URL), vars);
		} catch (Exception e) {
			// this may happen. we cannot do anything here
			System.out.println("");
		}
		return null;
	}

}