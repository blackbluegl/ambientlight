package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import android.os.AsyncTask;


public class UnregisterCallbackTask extends AsyncTask<String, Void, Void> {

	private final String URL = "/callback/{room}/client";


	@Override
	protected Void doInBackground(String... params) {

		Map<String, String> vars = Collections.singletonMap("room", params[0]);

		try {
			Rest.getRestTemplate().delete(Rest.getUrl(URL), params[1], vars);
		} catch (Exception e) {
			// this may happen. we cannot do anything here
		}
		return null;
	}

}