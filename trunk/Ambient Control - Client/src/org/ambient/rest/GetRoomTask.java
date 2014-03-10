package org.ambient.rest;

import java.util.HashSet;
import java.util.Set;

import org.ambientlight.ws.Room;
import org.ambientlight.ws.SwitchableIdModule;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;


public class GetRoomTask extends AsyncTask<String, Void, Object> {

	private static final String LOG = "GetRoomTask";

	private final String URL = "/config/room";


	@Override
	protected Room doInBackground(String... params) {
		Log.i(LOG, "getRoomConfiguration is called");

		String url = URLUtils.getBaseUrl(params[0]) + URL;

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new SwitchableIdModule());

		// set a custom filter
		Set<String> filterProperties = new HashSet<String>();
		FilterProvider filters = new SimpleFilterProvider().addFilter("apiFilter",
				SimpleBeanPropertyFilter.serializeAllExcept(filterProperties));
		mapper.setFilters(filters);

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);
		MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter();
		conv.setObjectMapper(mapper);
		restTemplate.getMessageConverters().add(conv);
		Room response = null;
		try {
			response = restTemplate.getForObject(url, Room.class, "");
		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
			response = null;
		}
		return response;
	}
}