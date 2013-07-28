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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.TreeMap;

import org.ambient.control.R;
import org.ambient.views.adapter.EditConfigMapAdapter;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import de.devmil.common.ui.color.ColorSelectorView;
import de.devmil.common.ui.color.ColorSelectorView.OnColorChangedListener;


/**
 * @author Florian Bornkessel
 * 
 */
public class EditConfigHandlerFragment extends Fragment {

	public static String CREATE_MODE = "creationMode";
	public static String CLASS_NAME = "className";
	public static String OBJECT_VALUE = "objectValue";

	public Object myConfigurationData;

	private boolean createMode = false;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (getArguments().containsKey(CREATE_MODE)) {
			createMode = getArguments().getBoolean(CREATE_MODE);
		}
		if (createMode) {
			try {
				myConfigurationData = Class.forName(getArguments().getString(CLASS_NAME)).newInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			myConfigurationData = getArguments().getSerializable(OBJECT_VALUE);
		}
		View result = null;
		try {
			result = this.createViewByObject(inflater, myConfigurationData);
		} catch (Exception e) {

		}
		return result;
	}


	public View createViewByObject(LayoutInflater inflater, final Object config) throws IllegalArgumentException,
	IllegalAccessException {

		LinearLayout content = new LinearLayout(this.getActivity());
		content.setOrientation(LinearLayout.VERTICAL);

		Map<Integer, Field> sortedMap = new TreeMap<Integer, Field>();

		for (final Field field : config.getClass().getFields()) {
			if (field.isAnnotationPresent(TypeDef.class)) {
				if (field.isAnnotationPresent(Presentation.class)) {
					Presentation presentation = field.getAnnotation(Presentation.class);
					sortedMap.put(Integer.parseInt(presentation.position()), field);
				} else {
					addFieldToView(inflater, config, content, field, null);
				}
			}
		}

		if (sortedMap.size() > 0) {
			for (Field field : sortedMap.values()) {
				addFieldToView(inflater, config, content, field, field.getAnnotation(Presentation.class).name());
			}
		}
		return content;
	}


	private void addFieldToView(LayoutInflater inflater, final Object config, LinearLayout content, final Field field, String name)
			throws IllegalAccessException {
		TypeDef description = field.getAnnotation(TypeDef.class);
		TextView label = new TextView(content.getContext());
		if (name != null) {
			label.setText(name);
		} else {
			label.setText(field.getName());
		}
		content.addView(label);

		if (description.fieldType().equals(FieldType.COLOR)) {
			ColorSelectorView colorView = new ColorSelectorView(content.getContext());
			colorView.setColor(field.getInt(config));
			content.addView(colorView);
			colorView.setOnColorChangedListener(new OnColorChangedListener() {

				@Override
				public void colorChanged(int color) {
					try {
						field.setInt(config, color);
					} catch (Exception e) {
						// this should not happen
					}
				}
			});
		}

		if (description.fieldType().equals(FieldType.NUMERIC)) {
			final double min = Double.parseDouble(description.min());
			final double difference = Double.parseDouble(description.max()) - min;

			SeekBar seekBar = new SeekBar(content.getContext());
			seekBar.setMax(256);
			double doubleValue = ((((Number) field.get(config)).doubleValue()) - min) / difference;
			seekBar.setProgress((int) (doubleValue * 256.0));
			content.addView(seekBar);
			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}


				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}


				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

					double result = (progress / 256.0) * difference + min;

					if (field.getType().equals(Double.TYPE)) {
						try {
							field.setDouble(config, result);
						} catch (Exception e) {
							// this should not happen
						}
					}

					if (field.getType().equals(Integer.TYPE)) {
						try {
							field.setInt(config, (int) result);
						} catch (Exception e) {
							// this should not happen
						}
					}
				}
			});
		}
		if (description.fieldType().equals(FieldType.MAP)) {
			LinearLayout mapViewHandler = (LinearLayout) inflater.inflate(R.layout.layout_map_list, content);
			ListView list = (ListView) mapViewHandler.findViewById(R.id.listView);
			ParameterizedType pt = (ParameterizedType) field.getGenericType();
			String containingClass = pt.getActualTypeArguments()[1].toString();
			list.setTag(containingClass);
			@SuppressWarnings("rawtypes")
			final Map map = (Map) field.get(config);

			final EditConfigMapAdapter adapter = new EditConfigMapAdapter(getFragmentManager(), getActivity(), map);
			list.setAdapter(adapter);

			list.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					view.setSelected(true);
					return true;
				}
			});

			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
					FragmentTransaction ft = getFragmentManager().beginTransaction();
					EditConfigHandlerFragment configHandler = new EditConfigHandlerFragment();
					ft.replace(R.id.LayoutMain, configHandler);
					ft.addToBackStack(null);
					Bundle args = new Bundle();
					configHandler.setArguments(args);
					String currentText = (String) ((TextView) paramView.findViewById(R.id.textViewName)).getText();
					args.putSerializable(EditConfigHandlerFragment.OBJECT_VALUE, (Serializable) map.get(currentText));
					ft.commit();

				}
			});

			list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			list.setMultiChoiceModeListener(new MultiChoiceModeListener() {

				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					// TODO Auto-generated method stub
					return false;
				}


				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.fragment_processcard_menu, menu);
					return true;

				}


				@Override
				public void onDestroyActionMode(ActionMode mode) {

				}


				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					// TODO Auto-generated method stub
					return false;
				}


				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
					// TODO Auto-generated method stub

				}
			});
		}
	}


	// @Override
	// public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo
	// menuInfo) {
	// super.onCreateContextMenu(menu, v, menuInfo);
	// if (v.getId() == R.id.listView) {
	// String className = (String) v.getTag();
	// Class classForAlternatives = null;
	// try {
	// classForAlternatives = Class.forName(className);
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// Alternatives alternatives = (Alternatives)
	// classForAlternatives.getAnnotation(Alternatives.class);
	// for (Alternative alternativeClassType : alternatives.alternatives()) {
	// menu.add(Menu.NONE, v.getId(), Menu.NONE, alternativeClassType.name());
	// }
	// }
	// }
}
