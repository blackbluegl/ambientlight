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
import org.ambient.control.config.classhandlers.BeanSelectionField;
import org.ambient.control.config.classhandlers.BooleanField;
import org.ambient.control.config.classhandlers.ColorField;
import org.ambient.control.config.classhandlers.ExpressionField;
import org.ambient.control.config.classhandlers.MapField;
import org.ambient.control.config.classhandlers.NumericField;
import org.ambient.control.config.classhandlers.SelectionListField;
import org.ambient.control.config.classhandlers.SimpleListField;
import org.ambient.control.config.classhandlers.StringField;
import org.ambient.control.config.classhandlers.WhereToMergeBean;
import org.ambient.control.config.classhandlers.WhereToMergeBean.WhereToPutType;
import org.ambient.util.GuiUtils;
import org.ambientlight.annotations.AlternativeClassValues;
import org.ambientlight.annotations.ClassDescription;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Group;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.ws.Room;

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
public class EditConfigHandlerFragment extends Fragment implements EditConfigOnExitListener {

	public static final String ARG_CREATE_MODE = "creationMode";
	public static final String ARG_CLASS_NAME = "className";
	public static final String BUNDLE_OBJECT_VALUE = "objectValue";
	public static final String ARG_SELECTED_ROOM = "selectedRoom";
	public static final String BUNDLE_WHERE_TO_INTEGRATE = "whereToIntegrate";
	public static final String ARG_ROOM_CONFIG = "roomConfiguration";

	protected static final String LOG = "org.ambientlight.EditConfigHandler";

	public static int REQ_RETURN_OBJECT = 0;

	protected Object myConfigurationData;

	protected Object valueToIntegrate = null;

	protected Room roomConfig = null;

	protected String selectedRoom = null;

	protected boolean createMode = false;

	public WhereToMergeBean whereToMergeChildBean = null;


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_edit_configuration_menu, menu);
	}


	@Override
	public void onSaveInstanceState(Bundle bundle) {
		bundle.putSerializable(BUNDLE_OBJECT_VALUE, (Serializable) myConfigurationData);
		bundle.putSerializable(BUNDLE_WHERE_TO_INTEGRATE, whereToMergeChildBean);
		super.onSaveInstanceState(bundle);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setDisplayShowHomeEnabled(true);

		setHasOptionsMenu(true);

		if (getArguments().isEmpty() == false) {
			createMode = getArguments().getBoolean(ARG_CREATE_MODE);
			selectedRoom = getArguments().getString(ARG_SELECTED_ROOM);
			this.roomConfig = (Room) getArguments().getSerializable(ARG_ROOM_CONFIG);
		}

		if (savedInstanceState != null) {
			myConfigurationData = savedInstanceState.getSerializable(BUNDLE_OBJECT_VALUE);
			whereToMergeChildBean = (WhereToMergeBean) savedInstanceState.getSerializable(BUNDLE_WHERE_TO_INTEGRATE);
		} else {
			if (createMode) {
				try {
					if (myConfigurationData == null) {
						myConfigurationData = Class.forName(getArguments().getString(ARG_CLASS_NAME)).newInstance();
					}
				} catch (Exception e) {
					Log.e(LOG, "could not create Object in createmode", e);
				}
			} else {
				myConfigurationData = GuiUtils.deepCloneSerializeable(getArguments().getSerializable(BUNDLE_OBJECT_VALUE));
			}
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// integrate object into existing configuration after child fragment
		// closes and has data for us via onIntegrate - callback we do the
		// integration here after the arguments and bundles have been restored.
		// the other way it would be possible that this instance here would be
		// created by android and all values would be null.
		if (this.valueToIntegrate != null) {
			this.integrateConfiguration(this.valueToIntegrate);
		}

		try {

			ScrollView result = (ScrollView) inflater.inflate(R.layout.fragment_edit_config, container, false);

			LinearLayout content = (LinearLayout) result.findViewById(R.id.linearLayoutEditConfigContent);

			// init the maps for grouped and sorted fields
			Map<Integer, Map<Integer, Field>> sortedMap = new TreeMap<Integer, Map<Integer, Field>>();
			// and a simple list for those fields which have no sort description
			List<Field> unsortedList = new ArrayList<Field>();

			// create groups if class description is present and defines some
			ClassDescription description = myConfigurationData.getClass().getAnnotation(ClassDescription.class);
			if (description != null) {
				for (Group currentGroup : description.groups()) {
					Map<Integer, Field> category = new TreeMap<Integer, Field>();
					sortedMap.put(currentGroup.position(), category);
				}
			} else {
				// create a default group
				sortedMap.put(0, new TreeMap<Integer, Field>());
			}

			for (final Field field : myConfigurationData.getClass().getFields()) {
				// only use field with typedef annotation. the rest of the
				// fields will be ignored
				if (field.isAnnotationPresent(TypeDef.class)) {
					if (field.isAnnotationPresent(Presentation.class)) {
						// put into groups
						Presentation presentation = field.getAnnotation(Presentation.class);
						sortedMap.get(presentation.groupPosition()).put(presentation.position(), field);
					} else {
						// or to unsorted group if no information for sorting is
						// present
						unsortedList.add(field);
					}
				}
			}

			// if no sorted values haven been drawn we create a default group on
			// screen and put all unsorted values in there. if some have been
			// drawn we put them into a "misc" group. we could do a check at the
			// beginning if several groups have been filled and so on. but we
			// render them first and get the information as result of that.
			boolean sortedValuesDrawn = false;

			for (Integer currentCategoryId : sortedMap.keySet()) {
				if (sortedMap.get(currentCategoryId).isEmpty()) {
					continue;
				}

				LinearLayout categoryView = (LinearLayout) inflater.inflate(R.layout.layout_group_header, null);
				TextView title = (TextView) categoryView.findViewById(R.id.textViewGroupHeader);
				TextView descriptionTextView = (TextView) categoryView.findViewById(R.id.textViewGroupDescription);
				content.addView(categoryView);

				if (currentCategoryId == 0) {
					// draw allgemein for category 0. if an group with id 0 is
					// described the values will be overwritten in next step
					title.setText("ALLGEMEIN");
					descriptionTextView.setVisibility(View.GONE);
				}

				// draw the category header and a description if present
				if (description != null) {
					for (Group currentGroup : description.groups()) {
						if (currentGroup.position() == currentCategoryId) {
							title.setText(currentGroup.name());
							if (currentGroup.description().isEmpty() == false) {
								descriptionTextView.setText(currentGroup.description());
								descriptionTextView.setVisibility(View.VISIBLE);
							} else {
								descriptionTextView.setVisibility(View.GONE);
							}
							break;
						}
					}
				}

				// draw all handlers for the fields in this category
				Map<Integer, Field> values = sortedMap.get(currentCategoryId);
				for (Field field : values.values()) {
					addFieldToView(inflater, myConfigurationData, content, field);

					// we are shure that the default or a special category have
					// been used. if there are unsorted values put them into an
					// additional group later
					sortedValuesDrawn = true;
				}
			}

			// draw a header for unsorted fields if class description provided
			// categories for the other fields
			if (sortedValuesDrawn && unsortedList.isEmpty() == false) {
				LinearLayout categoryFurther = (LinearLayout) inflater.inflate(R.layout.layout_group_header, null);
				TextView title = (TextView) categoryFurther.findViewById(R.id.textViewGroupHeader);
				title.setText("WEITERE");
				TextView descriptionTextView = (TextView) categoryFurther.findViewById(R.id.textViewGroupDescription);
				descriptionTextView.setVisibility(View.GONE);
				content.addView(categoryFurther);
			}

			// draw the handlers
			for (Field currentField : unsortedList) {
				addFieldToView(inflater, myConfigurationData, content, currentField);
			}

			// default text if no fields are annotated
			if (sortedValuesDrawn == false && unsortedList.isEmpty()) {
				TextView tv = new TextView(content.getContext());
				tv.setText("Dieses Objekt wird nicht konfiguriert");
				content.addView(tv);
			}

			return result;

		} catch (Exception e) {
			Log.e(LOG, "could not create View for bean: " + myConfigurationData.getClass().getName(), e);
			return null;
		}
	}


	private void addFieldToView(LayoutInflater inflater, final Object config, LinearLayout container, final Field field)
			throws IllegalAccessException, ClassNotFoundException, java.lang.InstantiationException {

		LinearLayout fieldView = (LinearLayout) inflater.inflate(R.layout.layout_editconfig_entry, null);
		container.addView(fieldView);

		TextView title = (TextView) fieldView.findViewById(R.id.textViewTitleOfMap);
		title.setText(field.getName());

		if (field.getAnnotation(Presentation.class) != null && field.getAnnotation(Presentation.class).name().isEmpty() == false) {
			title.setText((field.getAnnotation(Presentation.class).name()));

			final String description = (field.getAnnotation(Presentation.class)).description();
			if (description.isEmpty() == false) {
				// add a help button with the description of this field to title
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
		}

		// draw concrete ui elements for the field value
		LinearLayout contentArea = (LinearLayout) fieldView.findViewById(R.id.linearLayoutConfigEntryContent);
		TypeDef typedef = field.getAnnotation(TypeDef.class);

		if (typedef.fieldType().equals(FieldType.EXPRESSION)) {
			new ExpressionField(roomConfig, config, field, this, contentArea).createView();
		}

		if (typedef.fieldType().equals(FieldType.BEAN)) {
			new BeanField(roomConfig, config, field, this, contentArea).createView(selectedRoom);
		}

		if (typedef.fieldType().equals(FieldType.BEAN_SELECTION)) {
			new BeanSelectionField(roomConfig, config, field, this, contentArea).createView();
		}

		if (typedef.fieldType().equals(FieldType.STRING)) {
			new StringField(roomConfig, config, field, this, contentArea).createView();
		}

		if (typedef.fieldType().equals(FieldType.BOOLEAN)) {
			new BooleanField(roomConfig, config, field, this, contentArea).createView();
		}

		if (typedef.fieldType().equals(FieldType.COLOR)) {
			new ColorField(roomConfig, config, field, this, contentArea).createView();
		}

		if (typedef.fieldType().equals(FieldType.NUMERIC)) {
			new NumericField(roomConfig, config, field, this, contentArea).createView(typedef);
		}

		if (typedef.fieldType().equals(FieldType.MAP)) {
			new MapField(roomConfig, config, field, this, contentArea).createView(selectedRoom);
		}

		if (typedef.fieldType().equals(FieldType.SELECTION_LIST)) {
			new SelectionListField(roomConfig, config, field, this, contentArea).createView();
		}

		if (typedef.fieldType().equals(FieldType.SIMPLE_LIST)) {
			new SimpleListField(roomConfig, config, field, this, contentArea).createView(selectedRoom);
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuEntryFinishEditConfiguration:

			if (this.getTargetFragment() != null) {
				((EditConfigOnExitListener) this.getTargetFragment()).onIntegrateConfiguration(selectedRoom, myConfigurationData);
			}
			getFragmentManager().popBackStack();
			return true;

		case android.R.id.home:
			getFragmentManager().popBackStack();
			if (this.getTargetFragment() != null) {
				((EditConfigOnExitListener) this.getTargetFragment()).onRevertConfiguration(selectedRoom, myConfigurationData);
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}


	/*
	 * We are listener to our own interface. so we can descent into subobjects recursively. We store the value in a global
	 * variable. Later in lifecycle onCreate will be called. There we call integrateConfiguration(). This is needed because
	 * android does not guarantee that the instance would be kept with all global values until now.
	 * 
	 * @see org.ambient.control.processes.IntegrateObjectValueHandler# integrateConfiguration(java.lang.Object)
	 */
	@Override
	public void onIntegrateConfiguration(String roomName, Object configuration) {
		this.valueToIntegrate = configuration;
	}


	/*
	 * in that case we just do not copy the result into the config bean.
	 * 
	 * @see org.ambient.control.config.EditConfigExitListener#onRevertConfiguration (java.lang.String, java.lang.Object)
	 */
	@Override
	public void onRevertConfiguration(String roomName, Object configuration) {
		// do not merge the result

	}


	public void integrateConfiguration(Object configuration) {
		try {
			Class<? extends Object> myConfigurationClass = myConfigurationData.getClass();
			Field myField = myConfigurationClass.getField(whereToMergeChildBean.fieldName);

			if (whereToMergeChildBean.type.equals(WhereToPutType.FIELD)) {
				myField.set(myConfigurationData, configuration);

			} else if (whereToMergeChildBean.type.equals(WhereToPutType.LIST)) {
				@SuppressWarnings("unchecked")
				// checked in line above
				List<Object> list = (List<Object>) myField.get(myConfigurationData);
				if (whereToMergeChildBean.positionInList != null) {
					list.set(whereToMergeChildBean.positionInList, configuration);
				} else {
					list.add(configuration);
				}

			} else if (whereToMergeChildBean.type.equals(WhereToPutType.MAP)) {
				@SuppressWarnings("unchecked")
				// checked one line above
				Map<Object, Object> map = (Map<Object, Object>) myField.get(myConfigurationData);
				map.put(whereToMergeChildBean.keyInMap, configuration);
			}

		} catch (Exception e) {
			Log.e(LOG, "error trying to integrate data from child into configuration", e);
		}

		// we do not want to be called again
		this.valueToIntegrate = null;
	}


	/**
	 * helpermethod to start an fragment transaction and configure all values to display the fragment in editmode
	 * 
	 * @param sourceFragment
	 *            fragment to return to after editing is finished
	 * @param configValueToEdit
	 * @param selectedServer
	 * @param roomConfig
	 */
	public static void editConfigBean(Fragment sourceFragment, final Object configValueToEdit, final String selectedRoom,
			final Room roomConfig) {

		Bundle args = new Bundle();
		args.putString(ARG_SELECTED_ROOM, selectedRoom);
		args.putBoolean(ARG_CREATE_MODE, false);
		args.putSerializable(ARG_ROOM_CONFIG, roomConfig);
		args.putSerializable(EditConfigHandlerFragment.BUNDLE_OBJECT_VALUE, (Serializable) configValueToEdit);

		EditConfigHandlerFragment configHandler = createInstance();
		configHandler.setArguments(args);
		configHandler.setTargetFragment(sourceFragment, REQ_RETURN_OBJECT);

		FragmentTransaction ft = sourceFragment.getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
		ft.replace(R.id.LayoutMain, configHandler);
		ft.addToBackStack(null);
		ft.commit();
	}


	/**
	 * helper method to edit a config bean if alternative values are already known. E.g. Values are given by a referencing Field.
	 * 
	 * @param altValues
	 * @param alternativeValuesForDisplay
	 * @param fragment
	 * @param server
	 * @param roomConfig
	 */
	public static Bundle createNewConfigBean(final List<String> altValues, final CharSequence[] alternativeValuesForDisplay,
			final Fragment fragment, final String roomName, final Room roomConfig) {
		// As we cannot use a custom constructor, the bundle will be returned to add additional values from caller. E.g. for
		// subclasses.
		final Bundle args = new Bundle();

		AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
		builder.setTitle("Bitte auswählen").setItems(alternativeValuesForDisplay, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				args.putString(ARG_CLASS_NAME, altValues.get(which));
				args.putString(ARG_SELECTED_ROOM, roomName);
				args.putBoolean(ARG_CREATE_MODE, true);
				args.putSerializable(ARG_ROOM_CONFIG, roomConfig);

				EditConfigHandlerFragment configHandler = createInstance();
				configHandler.setTargetFragment(fragment, REQ_RETURN_OBJECT);

				configHandler.setArguments(args);

				FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
				ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
				ft.replace(R.id.LayoutMain, configHandler);
				ft.addToBackStack(null);
				ft.commit();
			}
		});

		builder.create().show();
		return args;
	}


	/**
	 * helpermethod to create a config bean from the scratch. The alternative values for concrete clases are guessed by classValue
	 * annotation from the class itself.
	 * 
	 * @param clazz
	 * @param fragment
	 * @param roomName
	 * @param roomConfiguration
	 * @throws ClassNotFoundException
	 * @throws java.lang.InstantiationException
	 * @throws IllegalAccessException
	 */
	public static <T> Bundle createNewConfigBean(Class<T> clazz, final Fragment fragment, final String roomName,
			final Room roomConfiguration) throws ClassNotFoundException, java.lang.InstantiationException, IllegalAccessException {

		org.ambientlight.annotations.valueprovider.api.AlternativeClassValues alternatives = ValueBindingHelper
				.getValuesForClass(clazz.getAnnotation(AlternativeClassValues.class));

		return createNewConfigBean(alternatives.classNames, ValueBindingHelper.toCharSequenceArray(alternatives.displayValues),
				fragment, roomName, roomConfiguration);
	}


	protected static EditConfigHandlerFragment createInstance() {
		return new EditConfigHandlerFragment();
	}
}
