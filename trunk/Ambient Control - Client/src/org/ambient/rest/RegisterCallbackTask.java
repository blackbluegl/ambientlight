package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;


public class RegisterCallbackTask extends AsyncTask<String, Void, Boolean> {

	private static final String LOG = RegisterCallbackTask.class.getName();

	private final String URL = "/callback/{room}/client";


	@Override
	protected Boolean doInBackground(String... params) {

		try {
			Map<String, String> vars = Collections.singletonMap("room", params[0]);

			Rest.getRestTemplate().put(Rest.getUrl(URL), params[1], vars);

			return true;

		} catch (Exception e) {

			Log.e(LOG, e.getMessage());

			return false;
		}
	}
}