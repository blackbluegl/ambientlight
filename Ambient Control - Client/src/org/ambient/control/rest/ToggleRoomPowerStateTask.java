package org.ambient.control.rest;

import org.ambient.control.home.HomeRefreshCallback;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;


public class ToggleRoomPowerStateTask extends AsyncTask<Object, Void, Void> {

	private final String URL = "/sceneryControl/control/room/state";


	private HomeRefreshCallback callback;
	private String serverName;


	@Override
	protected Void doInBackground(Object... params) {

		this.callback = (HomeRefreshCallback) params[2];
		this.serverName = (String) params[0];
		
		String url = URLUtils.getBaseUrl((String) params[0]) + URL;

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);

		// RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

		// restTemplate.postForEntity(url, params[1], Void.class);
		try {
			restTemplate.put(url, params[1]);
		} catch (Exception e) {

		}
		return null;
	}
	

	@Override
	protected void onPostExecute(Void result) {
		try {
			if(callback != null){
				callback.refreshRoomContent(serverName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}