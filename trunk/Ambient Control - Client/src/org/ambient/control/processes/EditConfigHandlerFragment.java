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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.ambient.control.MainActivity;
import org.ambient.control.R;
import org.ambient.views.adapter.EditConfigMapAdapter;
import org.ambientlight.annotations.AlternativeIds;
import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.room.RoomConfiguration;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

	private static enum WhereToPutType {
		FIELD, MAP, LIST
	}

	private class WhereToPutConfigurationData {

		public String fieldName;
		public String keyInMap;
		public int positionInList;
		public WhereToPutType type;
	}

	public static String CREATE_MODE = "creationMode";
	public static String CLASS_NAME = "className";
	public static String OBJECT_VALUE = "objectValue";
	public static String SELECTED_SERVER = "selectedServer";

	public static int REQ_RETURN_OBJECT = 0;

	public Object myConfigurationData;

	private String selectedServer = null;
	private boolean createMode = false;
	private WhereToPutConfigurationData whereToPutDataFromChild = null;


	public void persistConfigurationFromChild(Object configuration) throws IllegalArgumentException, IllegalAccessException,
	NoSuchFieldException {
		Class myObjectClass = myConfigurationData.getClass();
		Field myField = myObjectClass.getField(whereToPutDataFromChild.fieldName);
		if (whereToPutDataFromChild.type.equals(WhereToPutType.FIELD)) {
			myField.set(myConfigurationData, configuration);
		} else if (whereToPutDataFromChild.type.equals(WhereToPutType.LIST)) {
			List list = (List) myField.get(myConfigurationData);
			if (whereToPutDataFromChild.positionInList != 0) {
				list.set(whereToPutDataFromChild.positionInList, configuration);
			} else {
				list.add(configuration);
			}
		} else if (whereToPutDataFromChild.type.equals(WhereToPutType.MAP)) {
			Map map = (Map) myField.get(myConfigurationData);
			map.put(whereToPutDataFromChild.keyInMap, configuration);
		}
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_edit_configuration_menu, menu);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);
		// ActionBar actionBar = getActivity().getActionBar();
		// actionBar.show();
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

		selectedServer = getArguments().getString(SELECTED_SERVER);

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
		String fieldLabel = "";
		if (name != null) {
			fieldLabel = (name);
		} else {
			fieldLabel = field.getName();
		}

		if (description.fieldType().equals(FieldType.COLOR)) {
			TextView label = new TextView(content.getContext());
			label.setText(fieldLabel);
			content.addView(label);

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
			TextView label = new TextView(content.getContext());
			label.setText(fieldLabel);
			content.addView(label);

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
			TextView title = (TextView) mapViewHandler.findViewById(R.id.textViewTitleOfMap);
			title.setText(fieldLabel);

			RoomConfiguration roomConfig = ((MainActivity) getActivity()).getRoomConfigManager().getRoomConfiguration(
					selectedServer);
			AlternativeIds altIds = field.getAnnotation(AlternativeIds.class);
			List<String> additionalIds = ConfigBindingHelper.getArrayList(roomConfig, altIds.idBinding());

			final ListView list = (ListView) mapViewHandler.findViewById(R.id.listView);
			ParameterizedType pt = (ParameterizedType) field.getGenericType();
			final String containingClass = pt.getActualTypeArguments()[1].toString();
			list.setTag(containingClass);

			final AlternativeValues altValues = field.getAnnotation(AlternativeValues.class);
			final CharSequence[] alternativeValues = new CharSequence[altValues.values().length];
			for (int i = 0; i < alternativeValues.length; i++) {
				alternativeValues[i] = altValues.values()[i].name();
			}

			@SuppressWarnings("rawtypes")
			final Map map = (Map) field.get(config);

			for (Object key : additionalIds) {
				if (map.containsKey(key) == false) {
					map.put(key, null);
				}
			}

			final EditConfigMapAdapter adapter = new EditConfigMapAdapter(getFragmentManager(), getActivity(), map);
			list.setAdapter(adapter);
			list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

			final Fragment myself = this;
			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {

					Object valueAtPosition = adapter.getItem(paramInt).getValue();

					WhereToPutConfigurationData whereToStore = new WhereToPutConfigurationData();
					whereToStore.fieldName = field.getName();
					whereToStore.type = WhereToPutType.MAP;
					whereToStore.keyInMap = adapter.getItem(paramInt).getKey();
					whereToPutDataFromChild = whereToStore;

					if (valueAtPosition == null) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle("Bitte auswählen").setItems(alternativeValues, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								Bundle args = new Bundle();
								args.putString(CLASS_NAME, altValues.values()[which].className());
								args.putString(SELECTED_SERVER, selectedServer);
								args.putBoolean(CREATE_MODE, true);
								FragmentTransaction ft = getFragmentManager().beginTransaction();
								EditConfigHandlerFragment configHandler = new EditConfigHandlerFragment();
								ft.replace(R.id.LayoutMain, configHandler);
								ft.addToBackStack(null);
								configHandler.setArguments(args);
								configHandler.setTargetFragment(myself, REQ_RETURN_OBJECT);
								ft.commit();
							}
						});
						builder.create().show();
					} else {
						FragmentTransaction ft = getFragmentManager().beginTransaction();
						EditConfigHandlerFragment configHandler = new EditConfigHandlerFragment();
						ft.replace(R.id.LayoutMain, configHandler);
						ft.addToBackStack(null);
						Bundle args = new Bundle();
						configHandler.setArguments(args);
						String currentText = (String) ((TextView) paramView.findViewById(R.id.textViewName)).getText();
						args.putSerializable(EditConfigHandlerFragment.OBJECT_VALUE, (Serializable) map.get(currentText));
						args.putString(SELECTED_SERVER, selectedServer);
						ft.commit();
					}

				}
			});

			list.setMultiChoiceModeListener(new MultiChoiceModeListener() {

				List<Integer> checkedItems = new ArrayList<Integer>();


				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

					switch (item.getItemId()) {

					case R.id.menuEntryRemoveConfigurationClass:

						List<Entry<String, Object>> list = new ArrayList<Entry<String, Object>>();
						for (Integer position : checkedItems) {
							list.add(adapter.getItem(position));
						}

						for (Entry<String, Object> current : list) {
							adapter.remove(current);
							map.remove(current.getKey());
						}
						adapter.notifyDataSetChanged();
						break;
					}
					return false;
				}


				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.fragment_edit_configuration_cab, menu);
					// menu.add("test");
					return true;

				}


				@Override
				public void onDestroyActionMode(ActionMode mode) {
					checkedItems.clear();
				}


				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					// TODO Auto-generated method stub
					return false;
				}


				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
					if (checked) {
						checkedItems.add(position);
					} else {
						checkedItems.remove(position);
					}
					mode.setTitle(checkedItems.size() + " ausgewählt");
				}
			});
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuEntryCancelEditConfiguration:
			getFragmentManager().popBackStack();
			return true;
		case R.id.menuEntryFinishEditConfiguration:
			try {
				((EditConfigHandlerFragment) getTargetFragment()).persistConfigurationFromChild(myConfigurationData);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getFragmentManager().popBackStack();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
