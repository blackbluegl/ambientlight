package org.ambient.control.rest;

import org.ambientlight.scenery.entities.configuration.LightObjectConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;

public class ToggleLightObjectPowerStateTask  extends AsyncTask<Object,Void, LightObjectConfiguration>{

	private final String URL= "/sceneryControl/control/room/lightObjects/";
	private final String URL2="/state";
		@Override
		protected LightObjectConfiguration doInBackground(Object... params) {
			
			String url = URLUtils.getBaseUrl((String) params[0])+URL+(String) params[1]+URL2;
			
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			RestTemplate restTemplate = new RestTemplate(true, requestFactory);
			
			restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
			
			ResponseEntity<LightObjectConfiguration> response =  restTemplate.postForEntity(url, params[2], LightObjectConfiguration.class);
			LightObjectConfiguration result = response.getBody();
			
			return result;
		}
	}