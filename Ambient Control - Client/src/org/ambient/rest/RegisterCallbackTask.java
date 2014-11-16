package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import org.ambient.rest.callbacks.RegisterCallbackResultHandler;

import android.os.AsyncTask;
import android.util.Log;


public class RegisterCallbackTask extends AsyncTask<Object, Void, Boolean> {

	private static final String LOG = RegisterCallbackTask.class.getName();

	private final String URL = "/callback/{room}/client";

	private RegisterCallbackResultHandler resultHandler = null;
	private String roomName = null;


	@Override
	protected Boolean doInBackground(Object... params) {

		this.resultHandler = (RegisterCallbackResultHandler) params[2];
		this.roomName = (String) params[0];

		try {
			Map<String, String> vars = Collections.singletonMap("room", roomName);

			Rest.getRestTemplate().put(Rest.getUrl(URL), params[1], vars);

			return true;
		} catch (Exception e) {

			Log.e(LOG, e.getMessage());
			return false;
		}
	}


	@Override
	protected void onPostExecute(Boolean result) {
		resultHandler.onRegisterResult(this.roomName, result);
	}
}