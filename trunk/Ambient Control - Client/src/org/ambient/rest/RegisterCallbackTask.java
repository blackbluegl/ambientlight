package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import android.os.AsyncTask;


public class RegisterCallbackTask extends AsyncTask<String, Void, Boolean> {

	private final String URL = "/callback/{room}/client";


	@Override
	protected Boolean doInBackground(String... params) {

		String url = Rest.getBaseUrl(params[0]) + URL;
		Map<String, String> vars = Collections.singletonMap("room", params[1]);

		try {
			Rest.getRestTemplate().put(url, params[2], vars);
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}