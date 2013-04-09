package org.ambient.control.rest;

import org.ambientlight.room.RoomConfiguration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;

public class ToggleRoomPowerStateTask extends
		AsyncTask<Object, Void, RoomConfiguration> {

	private final String URL = "/sceneryControl/control/room/state";

	@Override
	protected RoomConfiguration doInBackground(Object... params) {

		String url = URLUtils.getBaseUrl((String) params[0]) + URL;

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);
		
	//	RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(
				new MappingJacksonHttpMessageConverter());

		// restTemplate.postForEntity(url, params[1], Void.class);
		restTemplate.put(url, params[1]);
		return null;
	}
}