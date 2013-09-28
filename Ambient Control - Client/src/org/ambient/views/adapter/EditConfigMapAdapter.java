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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;


/**
 * @author Florian Bornkessel
 * 
 */
public class EditConfigMapAdapter extends ArrayAdapter<Map.Entry<String, Object>> {

	private final Context context;
	private final Map<String, Object> arrayMap;
	private final Map values;
	private final String valueClassType;


	public EditConfigMapAdapter(FragmentManager fm, Context context, Map<String, Object> arrayMap, Map values,
			String valueClassType) {
		super(context, R.layout.layout_map_list_entry);
		this.context = context;
		this.values = values;
		this.arrayMap = arrayMap;
		for (Map.Entry<String, Object> currentEntry : arrayMap.entrySet()) {
			super.add(currentEntry);
		}
		this.valueClassType = valueClassType;
	}


	public void removeAt(int position) {
		Map.Entry<String, Object> entry = super.getItem(position);
		super.remove(entry);
		arrayMap.remove(entry.getKey());
	}


	@Override
	public void remove(Map.Entry<String, Object> value) {
		super.remove(value);
		arrayMap.remove(value.getKey());
	}


	@Override
	public void add(Map.Entry<String, Object> value) {
		super.add(value);
		arrayMap.put(value.getKey(), value.getValue());
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final String currentKeyValue = super.getItem(position).getKey();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = null;
		if (valueClassType.equals(Boolean.class.getName())) {
			rowView = inflater.inflate(R.layout.layout_map_list_entry_boolean, parent, false);
			final TextView textIsSet = (TextView) rowView.findViewById(R.id.textViewType);
			CheckBox checkbox = (CheckBox) rowView.findViewById(R.id.checkBoxMapListEntry);
			checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					arrayMap.put(currentKeyValue, isChecked);
					values.put(currentKeyValue, isChecked);
					textIsSet.setText("anwenden");
				}
			});
			if (arrayMap.get(currentKeyValue) != null) {
				textIsSet.setText("anwenden");
				checkbox.setChecked((Boolean) arrayMap.get(currentKeyValue));
			} else {
				textIsSet.setText("");
			}
		} else {
			rowView = inflater.inflate(R.layout.layout_map_list_entry, parent, false);
			if (arrayMap.get(currentKeyValue) != null) {
				TextView textViewType = (TextView) rowView.findViewById(R.id.textViewType);
				textViewType.setText(arrayMap.get(currentKeyValue).getClass().getSimpleName());
			}
		}
		TextView textViewKey = (TextView) rowView.findViewById(R.id.textViewName);
		textViewKey.setText(currentKeyValue);
		return rowView;
	}
}
