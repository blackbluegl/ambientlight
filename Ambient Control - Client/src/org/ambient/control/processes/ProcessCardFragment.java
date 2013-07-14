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

package org.ambient.control.processes;

import org.ambient.control.R;
import org.ambient.control.rest.RestClient;
import org.ambient.control.rest.URLUtils;
import org.ambient.views.ProcessCardDrawer;
import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.room.RoomConfiguration;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * @author Florian Bornkessel
 *
 */
public class ProcessCardFragment extends Fragment {

	View myView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ProcessConfiguration process = null;
		try {
			RoomConfiguration config = RestClient.getRoom(URLUtils.ANDROID_ADT_SERVERS[0]);
			process = config.processes.get(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.myView = inflater.inflate(R.layout.fragment_processcard, container, false);
		ProcessCardDrawer drawer = (ProcessCardDrawer) myView.findViewById(R.id.processCardDrawer);
		drawer.setProcess(process);
		return myView;
	}
}
