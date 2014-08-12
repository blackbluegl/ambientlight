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
import org.ambient.control.config.EditConfigActivity;
import org.ambient.control.navigation.NavigationActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.LinearLayout;


/**
 * @author Florian Bornkessel
 * 
 */
public class ProcessCardActivity extends NavigationActivity {

	private static final String FRAGMENT_TAG = "processCardFragment";


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			LinearLayout content = (LinearLayout) findViewById(R.id.navActionContentLinearLayout);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

			ProcessCardFragment fragment = new ProcessCardFragment();
			ft.add(content.getId(), fragment, FRAGMENT_TAG);
			ft.commit();
		}

	}


	@Override
	public void onActivityResult(int request, int response, Intent intent) {

		// handle config Edit
		if (request == EditConfigActivity.REQUEST_EDIT_ROOM_ITEM && response == Activity.RESULT_OK) {
			ProcessCardFragment fragment = (ProcessCardFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
			Object result = intent.getSerializableExtra(EditConfigActivity.EXTRA_RESULT_VALUE);
			String roomName = intent.getStringExtra(EditConfigActivity.EXTRA_ROOM);
			fragment.onIntegrateConfiguration(roomName, result);
		}
	}
}
