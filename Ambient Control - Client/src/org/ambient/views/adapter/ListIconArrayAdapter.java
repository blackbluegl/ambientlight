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

import org.ambient.control.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * @author Florian Bornkessel
 * 
 */

public class ListIconArrayAdapter extends ArrayAdapter<String> {

	private final Context context;
	private final String[] values;
	private final int[] drawables;


	public ListIconArrayAdapter(Context context, String[] values, int[] drawables) {
		super(context, R.layout.layout_iconarray_adapter_entry, values);
		this.context = context;
		this.values = values;
		this.drawables = drawables;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.layout_iconarray_adapter_entry, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.textViewIconArrayAdapterEntry);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.imageViewArrayAdapterEntry);
		textView.setText(values[position]);

		int resourceId = drawables[position];
		if (drawables != null && resourceId != 0) {
			imageView.setImageResource(resourceId);
		}

		return rowView;
	}
}
