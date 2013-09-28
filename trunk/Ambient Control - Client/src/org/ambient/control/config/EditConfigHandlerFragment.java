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

package org.ambient.control.config;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ambient.control.R;
import org.ambient.control.config.classhandlers.BeanField;
import org.ambient.control.config.classhandlers.BooleanField;
import org.ambient.control.config.classhandlers.ColorField;
import org.ambient.control.config.classhandlers.ExpressionField;
import org.ambient.control.config.classhandlers.MapField;
import org.ambient.control.config.classhandlers.NumericField;
import org.ambient.control.config.classhandlers.SelectionListField;
import org.ambient.control.config.classhandlers.SimpleListField;
import org.ambient.control.config.classhandlers.StringField;
import org.ambient.control.config.classhandlers.WhereToPutConfigurationData;
import org.ambient.control.config.classhandlers.WhereToPutConfigurationData.WhereToPutType;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


/**
 * @author Florian Bornkessel
 * 
 */
public class EditConfigHandlerFragment extends Fragment implements EditConfigExitListener {

	public static final String CREATE_MODE = "creationMode";
	public static final String CLASS_NAME = "className";
	public static final String OBJECT_VALUE = "objectValue";
	public static final String SELECTED_SERVER = "selectedServer";
	public static final String WHERE_TO_INTEGRATE = "whereToIntegrate";
	public static final String ROOM_CONFIG = "roomConfiguration";
	private static final String LOG = "org.ambientlight.EditConfigHandler";

	public static int REQ_RETURN_OBJECT = 0;

	public Object myConfigurationData;

	public Object valueToIntegrate = null;

	protected RoomConfiguration roomConfig = null;

	protected String selectedServer = null;
	private boolean createMode = false;
	public WhereToPutConfigurationData whereToPutDataFromChild = null;


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_edit_configuration_menu, menu);
	}


	@Override
	public void onSaveInstanceState(Bundle bundle) {
		bundle.putSerializable(OBJECT_VALUE, (Serializable) myConfigurationData);
		bundle.putSerializable(WHERE_TO_INTEGRATE, whereToPutDataFromChild);
		super.onSaveInstanceState(bundle);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setDisplayShowHomeEnabled(true);

		setHasOptionsMenu(true);
		if (getArguments().isEmpty() == false) {
			createMode = getArguments().getBoolean(CREATE_MODE);
			selectedServer = getArguments().getString(SELECTED_SERVER);
			this.roomConfig = (RoomConfiguration) getArguments().getSerializable(ROOM_CONFIG);
		}

		if (savedInstanceState != null) {
			myConfigurationData = savedInstanceState.getSerializable(OBJECT_VALUE);
			whereToPutDataFromChild = (WhereToPutConfigurationData) savedInstanceState.getSerializable(WHERE_TO_INTEGRATE);
		} else {
			if (createMode) {
				try {
					if (myConfigurationData == null) {
						myConfigurationData = Class.forName(getArguments().getString(CLASS_NAME)).newInstance();
					}
				} catch (Exception e) {
					Log.e("EditConfigHandler", "could not create Object in createmode", e);
				}
			} else {
				myConfigurationData = getArguments().getSerializable(OBJECT_VALUE);
			}
		}

		// integrate object into existing configuration
		if (this.valueToIntegrate != null) {
			this.integrateConfiguration(this.valueToIntegrate);
		}

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

		List<String> altValues = null;
		List<String> altValuesToDisplay = null;

		if (field.getAnnotation(AlternativeValues.class) != null) {
			altValues = ConfigBindingHelper.getAlternativeValues(field.getAnnotation(AlternativeValues.class), config.getClass()
					.getName(), roomConfig);
			altValuesToDisplay = ConfigBindingHelper.getAlternativeValuesForDisplay(field.getAnnotation(AlternativeValues.class),
					config.getClass().getName(), roomConfig);
		} else if (field.getDeclaringClass().getAnnotation(AlternativeValues.class) != null) {
			altValues = ConfigBindingHelper.getAlternativeValues(
					field.getDeclaringClass().getAnnotation(AlternativeValues.class), config.getClass().getName(), roomConfig);
			altValuesToDisplay = ConfigBindingHelper.getAlternativeValuesForDisplay(
					field.getDeclaringClass().getAnnotation(AlternativeValues.class), config.getClass().getName(), roomConfig);
		}

		TextView title = (TextView) fieldView.findViewById(R.id.textViewTitleOfMap);
		title.setText(fieldLabel);
		LinearLayout contentArea = (LinearLayout) fieldView.findViewById(R.id.linearLayoutConfigEntryContent);

		if (typedef.fieldType().equals(FieldType.EXPRESSION)) {
			ExpressionField.createView(this, config, container, field, altValues, contentArea);
		}

		if (typedef.fieldType().equals(FieldType.BEAN)) {
			BeanField.createView(this, config, field, altValues, altValuesToDisplay, contentArea, selectedServer, roomConfig);
		}

		if (typedef.fieldType().equals(FieldType.STRING)) {
			StringField.createView(this, config, container, field, altValues, altValuesToDisplay, contentArea);
		}

		if (typedef.fieldType().equals(FieldType.BOOLEAN)) {
			BooleanField.createView(config, container, field, contentArea);
		}

		if (typedef.fieldType().equals(FieldType.COLOR)) {
			ColorField.createView(config, container, field, contentArea);
		}

		if (typedef.fieldType().equals(FieldType.NUMERIC)) {
			NumericField.createView(config, container, field, typedef, contentArea);
		}

		if (typedef.fieldType().equals(FieldType.MAP)) {
			MapField.createView(this, config, field, altValues, altValuesToDisplay, contentArea, selectedServer, roomConfig);
		}

		if (typedef.fieldType().equals(FieldType.SELECTION_LIST)) {

			SelectionListField.createView(this, config, field, altValues, contentArea);
		}
		if (typedef.fieldType().equals(FieldType.SIMPLE_LIST)) {

			SimpleListField.createView(this, config, field, altValues, contentArea, selectedServer, roomConfig);
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuEntryFinishEditConfiguration:
			try {
				if (this.getTargetFragment() != null) {
					((EditConfigExitListener) this.getTargetFragment()).onIntegrateConfiguration(selectedServer,
							myConfigurationData);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			getFragmentManager().popBackStack();
			return true;

		case android.R.id.home:
			getFragmentManager().popBackStack();
			if (this.getTargetFragment() != null) {
				((EditConfigExitListener) this.getTargetFragment()).onRevertConfiguration(selectedServer, myConfigurationData);
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}


	/*
	 * callback on targetfragment. we store the value in a global variable and
	 * later in lifecycle onCreate will be called. there we call
	 * integrateConfiguration
	 * 
	 * @see org.ambient.control.processes.IntegrateObjectValueHandler#
	 * integrateConfiguration(java.lang.Object)
	 */
	@Override
	public void onIntegrateConfiguration(String serverName, Object configuration) {
		this.valueToIntegrate = configuration;
	}


	public void integrateConfiguration(Object configuration) {
		try {
			Class<? extends Object> myObjectClass = myConfigurationData.getClass();
			Field myField = myObjectClass.getField(whereToPutDataFromChild.fieldName);
			if (whereToPutDataFromChild.type.equals(WhereToPutType.FIELD)) {
				myField.set(myConfigurationData, configuration);
			} else if (whereToPutDataFromChild.type.equals(WhereToPutType.LIST)) {
				@SuppressWarnings("unchecked")
				// checked in line above
				List<Object> list = (List<Object>) myField.get(myConfigurationData);
				if (whereToPutDataFromChild.positionInList != 0) {
					list.set(whereToPutDataFromChild.positionInList, configuration);
				} else {
					list.add(configuration);
				}
			} else if (whereToPutDataFromChild.type.equals(WhereToPutType.MAP)) {
				@SuppressWarnings("unchecked")
				// checked one line above
				Map<String, Object> map = (Map<String, Object>) myField.get(myConfigurationData);
				map.put(whereToPutDataFromChild.keyInMap, configuration);
			}
		} catch (Exception e) {
			Log.e(LOG, "error trying to integrate data from child into configuration", e);
		}
		this.valueToIntegrate = null;
	}


	/**
	 * @param config
	 * @param field
	 * @throws IllegalAccessException
	 */
	public static void editConfigBean(Fragment fragment, final Object configValueToEdit, final String selectedServer,
			final RoomConfiguration roomConfig) {
		FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
		EditConfigHandlerFragment configHandler = new EditConfigHandlerFragment();
		ft.replace(R.id.LayoutMain, configHandler);
		configHandler.setTargetFragment(fragment, REQ_RETURN_OBJECT);
		ft.addToBackStack(null);
		Bundle args = new Bundle();
		configHandler.setArguments(args);
		args.putString(SELECTED_SERVER, selectedServer);
		args.putBoolean(CREATE_MODE, false);
		args.putSerializable(ROOM_CONFIG, roomConfig);
		args.putSerializable(EditConfigHandlerFragment.OBJECT_VALUE, (Serializable) configValueToEdit);
		args.putString(SELECTED_SERVER, selectedServer);
		ft.commit();
	}


	/**
	 * @param altValuesForListener
	 * @param alternativeValuesForDisplay
	 * @param myself
	 */
	public static void createNewConfigBean(final List<String> altValues, final CharSequence[] alternativeValuesForDisplay,
			final Fragment fragment, final String server, final RoomConfiguration roomConfig) {

		AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
		builder.setTitle("Bitte auswählen").setItems(alternativeValuesForDisplay, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Bundle args = new Bundle();
				args.putString(CLASS_NAME, altValues.get(which));
				args.putString(SELECTED_SERVER, server);
				args.putBoolean(CREATE_MODE, true);
				args.putSerializable(ROOM_CONFIG, roomConfig);
				FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
				ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
				EditConfigHandlerFragment configHandler = new EditConfigHandlerFragment();
				configHandler.setTargetFragment(fragment, REQ_RETURN_OBJECT);
				ft.replace(R.id.LayoutMain, configHandler);
				ft.addToBackStack(null);
				configHandler.setArguments(args);
				configHandler.setTargetFragment(fragment, REQ_RETURN_OBJECT);
				ft.commit();
			}
		});
		builder.create().show();
	}


	public static <T> void createNewConfigBean(Class<T> clazz, final Fragment fragment, final String server,
			final RoomConfiguration roomConfiguration) {

		List<String> altValues = ConfigBindingHelper.getAlternativeValues(clazz.getAnnotation(AlternativeValues.class),
				clazz.getName(), roomConfiguration);
		List<String> altValuesToDisplay = ConfigBindingHelper.getAlternativeValuesForDisplay(
				clazz.getAnnotation(AlternativeValues.class), clazz.getName(), roomConfiguration);
		createNewConfigBean(altValues, ConfigBindingHelper.toCharSequenceArray(altValuesToDisplay), fragment, server,
				roomConfiguration);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambient.control.config.EditConfigExitListener#onRevertConfiguration
	 * (java.lang.String, java.lang.Object)
	 */
	@Override
	public void onRevertConfiguration(String serverName, Object configuration) {
		// do not merge the result

	}

}
