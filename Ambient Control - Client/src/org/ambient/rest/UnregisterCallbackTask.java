package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import android.os.AsyncTask;


public class UnregisterCallbackTask extends AsyncTask<String, Void, Void> {

	private final String URL = "/callback/{room}/client";


	@Override
	protected Void doInBackground(String... params) {

		String url = Rest.getBaseUrl(params[0]) + URL;
		Map<String, String> vars = Collections.singletonMap("room", params[1]);

		try {
			Rest.getRestTemplate().delete(url, params[2], vars);
		} catch (Exception e) {
			// this may happen. we cannot do anything here
		}
		return null;
	}

}