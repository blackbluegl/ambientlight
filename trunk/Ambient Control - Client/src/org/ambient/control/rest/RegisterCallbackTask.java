package org.ambient.control.rest;

import org.ambient.control.RoomConfigManager;
import org.ambientlight.room.RoomConfiguration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;


public class RegisterCallbackTask extends AsyncTask<Object, Void, Void> {

	private final String URL = "/eventReceiver/event";

	private RoomConfigManager callback;
	private String serverName;


	@Override
	protected Void doInBackground(Object... params) {

		this.callback = (RoomConfigManager) params[2];
		this.serverName = (String) params[0];

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);
		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

		String eventSenderURL = URLUtils.getBaseUrl((String) params[0]) + URL;
		restTemplate.put(eventSenderURL, params[1]);

		return null;
	}


	@Override
	protected void onPostExecute(Void result) {
		try {
			if (callback != null) {
				RoomConfiguration config = RestClient.getRoom(serverName);
				callback.updateRoomConfiguration(serverName, config);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}