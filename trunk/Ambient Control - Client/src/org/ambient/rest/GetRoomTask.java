package org.ambient.rest;

import org.ambientlight.ws.Room;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.util.Log;


public class GetRoomTask extends AsyncTask<String, Void, Object> {

	private static final String LOG = "GetRoomTask";

	private final String URL = "/config/room";


	@Override
	protected Room doInBackground(String... params) {
		Log.i(LOG, "getRoomConfiguration is called");

		String url = URLUtils.getBaseUrl(params[0]) + URL;

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);

		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		Room response = null;
		try {
			response = restTemplate.getForObject(url, Room.class, "");
		} catch (Exception e) {
			response = null;
		}
		return response;
	}
}