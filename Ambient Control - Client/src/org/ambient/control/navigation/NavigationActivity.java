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

package org.ambient.control.navigation;

import org.ambient.control.R;
import org.ambient.control.RoomServiceAwareActivity;
import org.ambient.control.home.HomeActivity;
import org.ambient.control.processes.ProcessCardActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


/**
 * @author Florian Bornkessel
 * 
 */
public class NavigationActivity extends RoomServiceAwareActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();
		// actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();

		setContentView(R.layout.activity_process_card);

		createNavigationDrawer();

	}


	private void createNavigationDrawer() {

		DrawerLayout drawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		String[] values = new String[] { "Mein Ambiente", "Mein Klima", "Meine Prozesse", "NFC-Tag anlernen" };
		ListView drawerListView = (ListView) this.findViewById(R.id.homeLeftDrawer);

		drawerListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values));
		// // Set the list's click listener
		drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				final String navigation = (String) parent.getItemAtPosition(position);

				if (navigation.equals("Mein Ambiente")) {
					startActivity(HomeActivity.class);
				}
				if (navigation.equals("Meine Prozesse")) {
					startActivity(ProcessCardActivity.class);
				}
			}
		});
	}


	private void startActivity(Class<?> activityClass) {

		if (activityClass.equals(this.getClass()))
			return;

		Intent request = new Intent(this, activityClass);
		startActivity(request);
		overridePendingTransition(0, 0);
	}
}
