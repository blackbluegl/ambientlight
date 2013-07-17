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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Set;

import org.ambient.control.R;
import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;
import org.reflections.Reflections;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


/**
 * @author Florian Bornkessel
 * 
 */
public class ChooseActionHandlerFragment extends Fragment {

	ArrayList<String> contentValues = new ArrayList<String>();
	ListView content;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.initArray();

		this.content = (ListView) inflater.inflate(R.layout.fragment_actionhandler_chooser, container, false);

		// ListIconArrayAdapter adapter = new
		// ListIconArrayAdapter(this.getActivity(), content.toArray(new
		// String[0]), null);
		// listView.setAdapter(adapter);
		// final SceneryProgramChooserActivity myself = this;
		// listView.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view, int
		// position, long id) {
		// TextView result = (TextView)
		// view.findViewById(R.id.textViewSceneryChooserEntryLabel);
		//
		// values.putString("configType", valuesMap.get(result.getText()));
		// values.putString("title",result.getText().toString());
		//
		// FragmentManager fm = getSupportFragmentManager();
		// SceneryConfigEditDialogFragment editSceneryConfigFragment = new
		// SceneryConfigEditDialogFragment();
		//
		// editSceneryConfigFragment.setArguments(values);
		//
		// if (isLargeLayout) {
		// // The device is using a large layout, so show the fragment as a
		// dialog
		// editSceneryConfigFragment.show(fm, null);
		// } else {
		// // The screen is smaller, so show the fragment in a fullscreen
		// activity
		// Intent i = new Intent(myself, SceneryConfigEditDialogHolder.class);
		// i.putExtras(values);
		// startActivity(i);
		// }
		//
		// }
		// });
		// }






		return content;

	}


	private void initArray() {
		contentValues.clear();
		Reflections reflections = new Reflections("org.ambientlight.process.handler");
		Set<Class<? extends AbstractActionHandlerConfiguration>> modules = reflections
				.getSubTypesOf(AbstractActionHandlerConfiguration.class);
		for (Class current : modules) {
			if (Modifier.isAbstract(current.getModifiers()) == false) {
				contentValues.add(current.getSimpleName());
			}
		}
	}

}
