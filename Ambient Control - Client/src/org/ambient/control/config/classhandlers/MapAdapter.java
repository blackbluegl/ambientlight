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

package org.ambient.control.config.classhandlers;

import java.util.List;
import java.util.Map;

import org.ambient.control.R;
import org.ambient.control.config.classhandlers.MapField.ViewModel;

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
 * ArrayAdapter to for the MapField. It creates a dual row layout for the key and value. Boolean values have a separate layout and
 * a simple action handler to set boolean values directly to the data model.
 * 
 * @author Florian Bornkessel
 * 
 */
public class MapAdapter extends ArrayAdapter<MapField.ViewModel> {

	private final Context context;

	private Map<Object, Object> dataModell;

	/** class type of the value. the map adapter can handle some simple value types by itself and shows specialized views */
	private final String valueClassType;


	/**displayValues
	 * 
	 * @param fm
	 * @param context
	 * @param keyMapping
	 *            key to displayString map
	 * @param dataModell
	 * @param valueClassType
	 *            class type of value
	 */
	public MapAdapter(FragmentManager fm, Context context, List<MapField.ViewModel> modell, Map<Object, Object> dataModell,
			String valueClassType) {

		super(context, R.layout.layout_map_list_entry, modell);
		this.context = context;
		this.dataModell = dataModell;
		this.valueClassType = valueClassType;
	}


	@Override
	public void remove(ViewModel entry) {
		entry.value = null;
		dataModell.remove(entry.key);
		notifyDataSetChanged();
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final MapField.ViewModel item = getItem(position);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = null;

		// handle booleans directly with special view and and control
		if (valueClassType.equals(Boolean.class.getName())) {
			rowView = inflater.inflate(R.layout.layout_map_list_entry_boolean, parent, false);
			final TextView textIsSet = (TextView) rowView.findViewById(R.id.textViewType);
			CheckBox checkbox = (CheckBox) rowView.findViewById(R.id.checkBoxMapListEntry);

			if (item.value != null) {
				// check box and show text
				textIsSet.setText("anwenden");
				checkbox.setChecked((Boolean) item.value);
			} else {
				textIsSet.setText("");
			}

			// handle clicks by itself.
			checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					item.value = isChecked;
					textIsSet.setText("anwenden");
				}
			});

		}

		// display other values with simple toString representation
		else {
			rowView = inflater.inflate(R.layout.layout_map_list_entry, parent, false);
			TextView textViewType = (TextView) rowView.findViewById(R.id.textViewType);
			if (item.value != null) {
				textViewType.setText(item.value.getClass().getSimpleName());
			} else {
				textViewType.setText("");
			}
		}

		// always display key as simple text
		TextView textViewKey = (TextView) rowView.findViewById(R.id.textViewName);
		textViewKey.setText(item.displayKey);

		return rowView;
	}
}
