package org.ambient.control.rest;

import org.ambientlight.room.RoomConfiguration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.util.Log;


public class GetRoomTask extends AsyncTask<String, Void, Object> {

	private static final String LOG = "GetRoomTask";

	private final String URL = "/sceneryControl/config/room";


	@Override
	protected RoomConfiguration doInBackground(String... params) {
		Log.i(LOG, "getRoomConfiguration is called");

		String url = URLUtils.getBaseUrl(params[0]) + URL;

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);

		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		RoomConfiguration response = null;
		try {
			response = restTemplate.getForObject(url, RoomConfiguration.class, "");
		} catch (Exception e) {
			response = null;
		}
		return response;
	}
}