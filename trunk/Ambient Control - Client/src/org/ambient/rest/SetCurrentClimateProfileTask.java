/*  Copyright 2013 Florian Bornkessel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.util.Log;


/**
 * @author Florian Bornkessel
 *
 */
public class SetCurrentClimateProfileTask extends AsyncTask<String, Void, Void> {

	private static final String LOG = SetCurrentClimateProfileTask.class.getName();

	private final String URL = "/config/climate/{roomName}/currentWeekProfile";


	@Override
	protected Void doInBackground(String... params) {

		try {
			String url = Rest.getUrl(URL);
			Map<String, String> vars = Collections.singletonMap("roomName", params[0]);

			RestTemplate restTemplate = Rest.getRestTemplate();

			restTemplate.put(url, params[1], vars);
		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
		}

		return null;
	}
}
