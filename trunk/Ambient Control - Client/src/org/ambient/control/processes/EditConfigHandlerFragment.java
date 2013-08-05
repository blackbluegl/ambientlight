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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.ambient.control.MainActivity;
import org.ambient.control.R;
import org.ambient.util.GuiUtils;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
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
			result = this.createViewByObject(inflater, myConfigurationData, container);
		} catch (Exception e) {
			Log.e("editConfigHandler", "an Exception has been raised", e);
		}
		return result;
	}


	public View createViewByObject(LayoutInflater inflater, final Object config, ViewGroup container)
			throws IllegalArgumentException, IllegalAccessException {

		ScrollView scrollView = (ScrollView) inflater.inflate(R.layout.fragment_edit_config, container, false);

		LinearLayout content = (LinearLayout) scrollView.findViewById(R.id.linearLayoutEditConfigContent);

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
		return scrollView;
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

		RoomConfiguration roomConfig = ((MainActivity) getActivity()).getRoomConfigManager().getRoomConfiguration(selectedServer);

		LinearLayout fieldView = (LinearLayout) inflater.inflate(R.layout.layout_editconfig_entry, null);
		content.addView(fieldView);
		TextView title = (TextView) fieldView.findViewById(R.id.textViewTitleOfMap);
		title.setText(fieldLabel);
		LinearLayout contentArea = (LinearLayout) fieldView.findViewById(R.id.linearLayoutConfigEntryContent);

		if (description.fieldType().equals(FieldType.STRING)) {
			if (field.getAnnotation(AlternativeValues.class) != null) {
				// create spinner

				Spinner spinner = new Spinner(content.getContext());
				content.addView(spinner);
				final List<String> values = ConfigBindingHelper.getAlternativeValues(
						field.getAnnotation(AlternativeValues.class), roomConfig);
				List<String> valuesToDisplay = ConfigBindingHelper.getAlternativeValuesForDisplay(
						field.getAnnotation(AlternativeValues.class), roomConfig);

				final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
						android.R.layout.simple_dropdown_item_1line, valuesToDisplay);
				spinner.setAdapter(adapter);

				spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
						String valueToPaste = values.get(paramInt);
						try {
							field.set(config, valueToPaste);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}


					@Override
					public void onNothingSelected(AdapterView<?> paramAdapterView) {
						// TODO Auto-generated method stub

					}
				});

			} else {
				// create textfield
				final EditText input = new EditText(content.getContext());
				content.addView(input);
				input.setText((String) field.get(config));
				input.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
						try {
							field.set(config, input.getText().toString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}


					@Override
					public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
						// TODO Auto-generated method stub
					}


					@Override
					public void afterTextChanged(Editable paramEditable) {
						// TODO Auto-generated method stub

					}
				});
			}
		}

		if (description.fieldType().equals(FieldType.BOOLEAN)) {
			final CheckBox checkbox = new CheckBox(content.getContext());
			contentArea.addView(checkbox);
			checkbox.setChecked(field.getBoolean(config));
			if (checkbox.isChecked()) {
				checkbox.setText("aktiviert");
			} else {
				checkbox.setText("deaktiviert");
			}
			checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton paramCompoundButton, boolean paramBoolean) {
					try {
						field.setBoolean(config, paramBoolean);
						if (paramBoolean) {
							checkbox.setText("aktiviert");
						} else {
							checkbox.setText("deaktiviert");
						}

					} catch (Exception e) {
						// this should not happen
					}
				}
			});
		}

		if (description.fieldType().equals(FieldType.COLOR)) {

			ColorSelectorView colorView = new ColorSelectorView(content.getContext());
			colorView.setColor(field.getInt(config));
			contentArea.addView(colorView);
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
			// TextView label = new TextView(content.getContext());
			// label.setText(fieldLabel);
			// content.addView(label);

			final double min = Double.parseDouble(description.min());
			final double difference = Double.parseDouble(description.max()) - min;

			SeekBar seekBar = new SeekBar(content.getContext());
			seekBar.setMax(256);
			double doubleValue = ((((Number) field.get(config)).doubleValue()) - min) / difference;
			seekBar.setProgress((int) (doubleValue * 256.0));
			contentArea.addView(seekBar);
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

			List<String> additionalIds = ConfigBindingHelper.getAlternativeIds(field.getAnnotation(AlternativeIds.class),
					roomConfig);

			final ListView list = new ListView(contentArea.getContext());
			contentArea.addView(list);
			ParameterizedType pt = (ParameterizedType) field.getGenericType();
			final String containingClass = pt.getActualTypeArguments()[1].toString().substring(6);
			list.setTag(containingClass);

			@SuppressWarnings("rawtypes")
			final Map map = (Map) field.get(config);
			final Map arrayMap = new LinkedHashMap();

			for (Object key : additionalIds) {
				if (map.containsKey(key) == false) {
					arrayMap.put(key, null);
				} else {
					arrayMap.put(key, map.get(key));
				}
			}

			final EditConfigMapAdapter adapter = new EditConfigMapAdapter(getFragmentManager(), getActivity(), arrayMap,
					containingClass);
			list.setAdapter(adapter);
			list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			GuiUtils.setListViewHeightBasedOnChildren(list);
			final Fragment myself = this;

			final List<String> altValues = ConfigBindingHelper.getAlternativeValues(field.getAnnotation(AlternativeValues.class),
					roomConfig);

			final CharSequence[] alternativeValuesForDisplay = ConfigBindingHelper.toCharSequenceArray(ConfigBindingHelper
					.getAlternativeValuesForDisplay(field.getAnnotation(AlternativeValues.class), roomConfig));

			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {

					Object valueAtPosition = adapter.getItem(paramInt).getValue();

					WhereToPutConfigurationData whereToStore = new WhereToPutConfigurationData();
					whereToStore.fieldName = field.getName();
					whereToStore.type = WhereToPutType.MAP;
					whereToStore.keyInMap = adapter.getItem(paramInt).getKey();
					whereToPutDataFromChild = whereToStore;

					if (valueAtPosition == null && alternativeValuesForDisplay.length > 0) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle("Bitte auswählen").setItems(alternativeValuesForDisplay,
								new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								Bundle args = new Bundle();
								args.putString(CLASS_NAME, altValues.get(which));
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
					} else if (valueAtPosition != null) {
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
							current.setValue(null);
							map.remove(current.getKey());
						}
						adapter.notifyDataSetChanged();
						break;

					case R.id.menuEntryEditConfigurationClass:
						FragmentTransaction ft = getFragmentManager().beginTransaction();
						EditConfigHandlerFragment configHandler = new EditConfigHandlerFragment();
						ft.replace(R.id.LayoutMain, configHandler);
						ft.addToBackStack(null);
						Bundle args = new Bundle();
						configHandler.setArguments(args);
						args.putSerializable(EditConfigHandlerFragment.OBJECT_VALUE,
								(Serializable) adapter.getItem(checkedItems.get(0)).getValue());
						args.putString(SELECTED_SERVER, selectedServer);
						ft.commit();
						break;

					}
					return false;
				}


				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.fragment_edit_configuration_cab, menu);
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

					MenuItem editItem = mode.getMenu().findItem(R.id.menuEntryEditConfigurationClass);
					if (checkedItems.size() == 1 && adapter.getItem(checkedItems.get(0)).getValue() != null) {
						editItem.setVisible(true);
					} else {
						editItem.setVisible(false);
					}
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
