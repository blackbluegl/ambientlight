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
import org.ambientlight.annotations.ClassDescription;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Group;
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
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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
import android.widget.MultiAutoCompleteTextView;
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

		// init the maps for sorted views
		Map<Integer, Map<Integer, Field>> sortedMap = new TreeMap<Integer, Map<Integer, Field>>();
		List<Field> unsortedList = new ArrayList<Field>();
		ClassDescription description = config.getClass().getAnnotation(ClassDescription.class);
		if (description != null) {
			for (Group currentGroup : description.groups()) {
				Map<Integer, Field> category = new TreeMap<Integer, Field>();
				sortedMap.put(currentGroup.position(), category);
			}
		} else {
			Map<Integer, Field> defaultCategory = new TreeMap<Integer, Field>();
			sortedMap.put(0, defaultCategory);
		}

		for (final Field field : config.getClass().getFields()) {
			if (field.isAnnotationPresent(TypeDef.class)) {
				if (field.isAnnotationPresent(Presentation.class)) {
					Presentation presentation = field.getAnnotation(Presentation.class);
					sortedMap.get(presentation.groupPosition()).put(presentation.position(), field);
				} else {
					unsortedList.add(field);
				}
			}
		}

		boolean sortedValuesDrawn = false;

		for (Integer currentCategoryId : sortedMap.keySet()) {
			if (sortedMap.get(currentCategoryId).isEmpty() == false) {
				LinearLayout categoryView = (LinearLayout) inflater.inflate(R.layout.layout_group_header, null);
				TextView title = (TextView) categoryView.findViewById(R.id.textViewGroupHeader);
				TextView descriptionTextView = (TextView) categoryView.findViewById(R.id.textViewGroupDescription);
				content.addView(categoryView);
				if (currentCategoryId == 0) {
					// draw allgemein for category 0
					title.setText("ALLGEMEIN");
					descriptionTextView.setVisibility(View.GONE);
				} else {
					for (Group currentGroup : description.groups()) {
						if (currentGroup.position() == currentCategoryId) {
							title.setText(currentGroup.name());
							if (currentGroup.description().isEmpty() == false) {
								descriptionTextView.setText(currentGroup.description());
							} else {
								descriptionTextView.setVisibility(View.GONE);
							}
							break;
						}
					}
				}
				Map<Integer, Field> values = sortedMap.get(currentCategoryId);
				for (Field field : values.values()) {
					addFieldToView(inflater, config, content, field);
					sortedValuesDrawn = true;
				}
			}
		}

		// do the same for unsorted fields
		if (sortedValuesDrawn && unsortedList.isEmpty() == false) {
			LinearLayout categoryFurther = (LinearLayout) inflater.inflate(R.layout.layout_group_header, null);
			TextView title = (TextView) categoryFurther.findViewById(R.id.textViewGroupHeader);
			title.setText("WEITERE");
			TextView descriptionTextView = (TextView) categoryFurther.findViewById(R.id.textViewGroupDescription);
			descriptionTextView.setVisibility(View.GONE);
			content.addView(categoryFurther);
		}

		for (Field currentField : unsortedList) {
			addFieldToView(inflater, config, content, currentField);
		}

		if (sortedValuesDrawn == false && unsortedList.isEmpty()) {
			TextView tv = new TextView(content.getContext());
			tv.setText("Dieses Objekt wird nicht konfiguriert");
			content.addView(tv);
		}

		return scrollView;
	}


	private void addFieldToView(LayoutInflater inflater, final Object config, LinearLayout container, final Field field)
			throws IllegalAccessException {
		TypeDef typedef = field.getAnnotation(TypeDef.class);
		String fieldLabel = null;

		LinearLayout fieldView = (LinearLayout) inflater.inflate(R.layout.layout_editconfig_entry, null);
		container.addView(fieldView);

		if (field.getAnnotation(Presentation.class) != null && field.getAnnotation(Presentation.class).name().isEmpty() == false) {
			fieldLabel = (field.getAnnotation(Presentation.class).name());
			final String description = (field.getAnnotation(Presentation.class)).description();
			if (description.isEmpty() == false) {
				fieldView.findViewById(R.id.imageViewEditEntryHelp).setVisibility(View.VISIBLE);
				LinearLayout header = (LinearLayout) fieldView.findViewById(R.id.linearLayoutConfigEntryHeader);
				header.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle("Hilfe").setMessage(description).setPositiveButton("OK", null).create().show();
					}
				});
			}

		} else {
			fieldLabel = field.getName();
		}

		RoomConfiguration roomConfig = ((MainActivity) getActivity()).getRoomConfigManager().getRoomConfiguration(selectedServer);

		TextView title = (TextView) fieldView.findViewById(R.id.textViewTitleOfMap);
		title.setText(fieldLabel);
		LinearLayout contentArea = (LinearLayout) fieldView.findViewById(R.id.linearLayoutConfigEntryContent);

		if (typedef.fieldType().equals(FieldType.EXPRESSION)) {

			// create textfield
			final MultiAutoCompleteTextView input = new MultiAutoCompleteTextView(container.getContext());
			contentArea.addView(input);
			input.setText((String) field.get(config));
			List<String> variablesRawValues = ConfigBindingHelper.getAlternativeValues(
					field.getAnnotation(AlternativeValues.class), roomConfig);
			List<String> variablesEnrichedValues = new ArrayList<String>();
			variablesEnrichedValues.add("#{tokenValue}");
			for (String current : variablesRawValues) {
				variablesEnrichedValues.add("#{" + current + "}");
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
					android.R.layout.simple_dropdown_item_1line, variablesEnrichedValues);
			input.setAdapter(adapter);
			input.showDropDown();
			input.setThreshold(1);
			input.setTokenizer(new SpaceTokenizer());
			input.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
					try {
						field.set(config, input.getText().toString());
						input.showDropDown();
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

		if (typedef.fieldType().equals(FieldType.BEAN)) {
			final Object value = field.get(config);
			final TextView beanView = new TextView(contentArea.getContext());
			contentArea.addView(beanView);
			if (value != null) {
				beanView.setText(value.getClass().getName());
			} else {
				beanView.setText("kein Objekt gesetzt");
			}
			final List<String> altValues = ConfigBindingHelper.getAlternativeValues(field.getAnnotation(AlternativeValues.class),
					roomConfig);

			final CharSequence[] alternativeValuesForDisplay = ConfigBindingHelper.toCharSequenceArray(ConfigBindingHelper
					.getAlternativeValuesForDisplay(field.getAnnotation(AlternativeValues.class), roomConfig));
			final EditConfigHandlerFragment myself = this;
			beanView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					WhereToPutConfigurationData whereToStore = new WhereToPutConfigurationData();
					whereToStore.fieldName = field.getName();
					whereToStore.type = WhereToPutType.FIELD;
					whereToPutDataFromChild = whereToStore;

					if (value == null && alternativeValuesForDisplay.length > 0) {
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
										ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
								EditConfigHandlerFragment configHandler = new EditConfigHandlerFragment();
								ft.replace(R.id.LayoutMain, configHandler);
								ft.addToBackStack(null);
								configHandler.setArguments(args);
								configHandler.setTargetFragment(myself, REQ_RETURN_OBJECT);
								ft.commit();
							}
						});
						builder.create().show();
					} else if (value != null) {
						FragmentTransaction ft = getFragmentManager().beginTransaction();
						ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
						EditConfigHandlerFragment configHandler = new EditConfigHandlerFragment();
						ft.replace(R.id.LayoutMain, configHandler);
						ft.addToBackStack(null);
						Bundle args = new Bundle();
						configHandler.setArguments(args);
						args.putSerializable(EditConfigHandlerFragment.OBJECT_VALUE, (Serializable) value);
						args.putString(SELECTED_SERVER, selectedServer);
						ft.commit();
					}
				}
			});

			beanView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					getActivity().startActionMode(new ActionMode.Callback() {

						@Override
						public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
							return false;
						}


						@Override
						public void onDestroyActionMode(ActionMode mode) {

						}


						@Override
						public boolean onCreateActionMode(ActionMode mode, Menu menu) {
							MenuInflater inflater = mode.getMenuInflater();
							inflater.inflate(R.menu.fragment_edit_configuration_cab, menu);
							MenuItem editItem = mode.getMenu().findItem(R.id.menuEntryEditConfigurationClass);
							if (value != null) {
								editItem.setVisible(true);
							} else {
								editItem.setVisible(false);
							}
							return true;
						}


						@Override
						public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

							switch (item.getItemId()) {

							case R.id.menuEntryRemoveConfigurationClass:

								try {
									field.set(config, null);
								} catch (Exception e) {
									e.printStackTrace();
								}
								break;

							case R.id.menuEntryEditConfigurationClass:
								FragmentTransaction ft = getFragmentManager().beginTransaction();
								ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
								EditConfigHandlerFragment configHandler = new EditConfigHandlerFragment();
								ft.replace(R.id.LayoutMain, configHandler);
								ft.addToBackStack(null);
								Bundle args = new Bundle();
								configHandler.setArguments(args);
								args.putSerializable(EditConfigHandlerFragment.OBJECT_VALUE, (Serializable) value);
								args.putString(SELECTED_SERVER, selectedServer);
								ft.commit();
								break;

							}
							return false;
						}
					});
					return true;
				}
			});
		}

		if (typedef.fieldType().equals(FieldType.STRING)) {
			if (field.getAnnotation(AlternativeValues.class) != null) {
				// create spinner

				Spinner spinner = new Spinner(container.getContext());
				contentArea.addView(spinner);
				final List<String> values = ConfigBindingHelper.getAlternativeValues(
						field.getAnnotation(AlternativeValues.class), roomConfig);
				List<String> valuesToDisplay = ConfigBindingHelper.getAlternativeValuesForDisplay(
						field.getAnnotation(AlternativeValues.class), roomConfig);

				final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
						android.R.layout.simple_spinner_item, valuesToDisplay);
				adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
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
				final EditText input = new EditText(container.getContext());
				contentArea.addView(input);
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

		if (typedef.fieldType().equals(FieldType.BOOLEAN)) {
			final CheckBox checkbox = new CheckBox(container.getContext());
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

		if (typedef.fieldType().equals(FieldType.COLOR)) {

			ColorSelectorView colorView = new ColorSelectorView(container.getContext());
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

		if (typedef.fieldType().equals(FieldType.NUMERIC)) {
			// TextView label = new TextView(content.getContext());
			// label.setText(fieldLabel);
			// content.addView(label);

			final double min = Double.parseDouble(typedef.min());
			final double difference = Double.parseDouble(typedef.max()) - min;

			SeekBar seekBar = new SeekBar(container.getContext());
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

		if (typedef.fieldType().equals(FieldType.MAP)) {

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
			// we skip this if the values are simple and can be handled directly
			// on the view, like booleans
			if (containingClass.equals(Boolean.class.getName()) == false) {
				final List<String> altValues = ConfigBindingHelper.getAlternativeValues(
						field.getAnnotation(AlternativeValues.class), roomConfig);

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
											ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
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
							ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
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
			}
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
						ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
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
						checkedItems.remove(Integer.valueOf(position));
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
