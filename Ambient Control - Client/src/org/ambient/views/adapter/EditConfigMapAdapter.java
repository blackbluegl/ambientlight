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

package org.ambient.views.adapter;

import java.util.Map;

import org.ambient.control.R;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


/**
 * @author Florian Bornkessel
 * 
 */
public class EditConfigMapAdapter extends ArrayAdapter<Map.Entry<String, Object>> {

	private final Context context;
	private final Map<String, Object> map;


	public EditConfigMapAdapter(FragmentManager fm, Context context, Map<String, Object> map) {
		super(context, R.layout.layout_map_list_entry);
		this.context = context;
		this.map = map;
		for (Map.Entry<String, Object> currentEntry : map.entrySet()) {
			super.add(currentEntry);
		}
	}


	public void removeAt(int position) {
		Map.Entry<String, Object> entry = super.getItem(position);
		super.remove(entry);
		map.remove(entry.getKey());
	}


	@Override
	public void remove(Map.Entry<String, Object> value) {
		super.remove(value);
		map.remove(value.getKey());
	}


	@Override
	public void add(Map.Entry<String, Object> value) {
		super.add(value);
		map.put(value.getKey(), value.getValue());
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.layout_map_list_entry, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.textViewName);

		final String currentText = super.getItem(position).getKey();
		textView.setText(currentText);
		if (map.get(currentText) != null) {
			TextView textViewType = (TextView) rowView.findViewById(R.id.textViewType);
			textViewType.setText(map.get(currentText).getClass().getSimpleName());
		}

		return rowView;
	}
}
