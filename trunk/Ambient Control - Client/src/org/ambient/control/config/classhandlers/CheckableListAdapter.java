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

import org.ambient.control.R;
import org.ambient.control.config.classhandlers.SelectionListField.ViewModel;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;


/**
 * simple adapter to handle checkable lists.
 * 
 * @author Florian Bornkessel
 * 
 */
public class CheckableListAdapter extends ArrayAdapter<ViewModel> {

	private final List<ViewModel> viewModelList;
	private final List<Object> backingList;
	private final Activity context;


	public CheckableListAdapter(Activity context, List<ViewModel> viewModelList, List<Object> data) {
		super(context, R.layout.layout_checkable_list_item, viewModelList);

		this.viewModelList = viewModelList;
		this.context = context;
		this.backingList = data;
	}


	@Override
	public View getView(int pos, View convertView, ViewGroup parentView) {
		final ViewModel viewModel = viewModelList.get(pos);

		// create or recycle container
		View view = null;
		if (convertView == null) {
			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(R.layout.layout_checkable_list_item, null);
		} else {
			view = convertView;
		}

		// these views will be handled
		TextView text = (TextView) view.findViewById(R.id.textCheckableListItem);
		final CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkBoxCheckableListItem);

		// bind view model
		text.setText(viewModel.displayName);
		checkbox.setChecked(viewModel.isChecked);

		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// invert selection
				viewModel.isChecked = !viewModel.isChecked;
				checkbox.setChecked(viewModel.isChecked);

				// add or remove from backingList
				if (viewModel.isChecked) {
					backingList.add(viewModel.altValue);
				} else {
					backingList.remove(viewModel.altValue);
				}
			}
		});

		return view;
	}

}
