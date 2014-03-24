package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import android.os.AsyncTask;


public class RegisterCallbackTask extends AsyncTask<String, Void, Boolean> {

	private final String URL = "/callback/{room}/client";


	@Override
	protected Boolean doInBackground(String... params) {

		Map<String, String> vars = Collections.singletonMap("room", params[0]);

		try {
			Rest.getRestTemplate().put(Rest.getUrl(URL), params[1], vars);
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}