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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.ambient.control.R;
import org.ambient.views.adapter.ListIconArrayAdapter;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;


/**
 * @author Florian Bornkessel
 * 
 */
public class ChooseActionHandlerFragment extends Fragment {

	ArrayList<String> contentValues = new ArrayList<String>();
	LinearLayout content;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		try {
			this.initArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.content = (LinearLayout) inflater.inflate(R.layout.fragment_actionhandler_chooser, container, false);
		ListView listView = (ListView) content.findViewById(R.id.listViewActionHandlerChooser);

		ListIconArrayAdapter adapter = new ListIconArrayAdapter(this.getActivity(), contentValues.toArray(new String[0]), null);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String itemClicked = contentValues.get(position);
				EditConfigHandlerFragment editSceneryConfigFragment = new EditConfigHandlerFragment();
				Bundle values = new Bundle();
				values.putString(EditConfigHandlerFragment.CLASS_NAME, itemClicked);
				values.putBoolean(EditConfigHandlerFragment.CREATE_MODE, true);
				editSceneryConfigFragment.setArguments(values);

				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.LayoutMain, editSceneryConfigFragment);
				ft.addToBackStack(null);
				ft.commit();
			}
		});
		return content;
	}


	private void initArray() throws IOException {
		Resources res = getResources();
		String[] handlers = res.getStringArray(R.array.actionhandler_array);
		contentValues = new ArrayList<String>(Arrays.asList(handlers));
	}

}
