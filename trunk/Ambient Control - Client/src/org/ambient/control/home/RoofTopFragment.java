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

package org.ambient.control.home;

import java.util.List;

import org.ambient.control.R;
import org.ambient.control.rest.RestClient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * @author Florian Bornkessel
 * 
 */
public class RoofTopFragment extends Fragment {

	public interface RoofTopListener {
		public void onMainPowerSwitched(boolean powerstate);
	}

	public static final String BUNDLE_HOST_LIST = "hosts";

	private View myView;

	/*
	 * list of all roomServers which will be represented by a roomContainer
	 * within this fragment. The initialization is handled by a bundle.
	 */
	private List<String> roomServers;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		roomServers = getArguments().getStringArrayList(BUNDLE_HOST_LIST);
		this.myView = inflater.inflate(R.layout.fragment_home_rooftop, container, false);

		ImageView masterButton = (ImageView) myView.findViewById(R.id.imageViewMasterSwitch);

		masterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				for (String currentServerName : roomServers) {
					try {
						RestClient.setPowerStateForRoom(currentServerName, false, (HomeRefreshCallback) getActivity());
						((RoofTopListener) getActivity()).onMainPowerSwitched(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				updateMasterSwitchState(false);
			}
		});

		return myView;
	}


	public void updateMasterSwitchState(boolean powerState) {

		ImageView masterSwitch = (ImageView) this.myView.findViewById(R.id.imageViewMasterSwitch);

		if (powerState == true) {
			masterSwitch.setImageResource(R.drawable.ic_power_active);
		} else {
			masterSwitch.setImageResource(R.drawable.ic_power_disabled);
		}
	}
}
