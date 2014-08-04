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
import org.ambient.control.config.WhereToMergeBean.WhereToPutType;
import org.ambient.util.GuiUtils;
import org.ambientlight.annotations.AlternativeClassValues;
import org.ambientlight.annotations.ClassDescription;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Group;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.ws.Room;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
public class EditConfigFragment extends Fragment implements EditConfigOnExitListener {

	public static final String ARG_CLASS_NAME = "className";
	public static final String BUNDLE_OBJECT_VALUE = "objectValue";
	public static final String ARG_SELECTED_ROOM = "selectedRoom";
	public static final String BUNDLE_WHERE_TO_INTEGRATE = "whereToIntegrate";
	public static final String ARG_ROOM_CONFIG = "roomConfiguration";

	protected static final String LOG = "org.ambientlight.EditConfigHandler";
	protected static final String FRAGMENT_TAG = "editConfigFragment";

	public static int REQ_RETURN_OBJECT = 0;

	protected Object beanToEdit;

	protected Object valueToIntegrate = null;

	protected Room roomConfig = null;

	protected String selectedRoom = null;

	public WhereToMergeBean whereToMergeChildBean = null;


	/**
	 * wrappermethod to create a bean from the scratch just by annotations from a class definition of the bean.
	 * 
	 * @param clazz
	 * @param source
	 * @param roomName
	 * @param roomConfiguration
	 * @throws ClassNotFoundException
	 * @throws java.lang.InstantiationException
	 * @throws IllegalAccessException
	 */
	protected static <T> void editNewConfigBean(Class<T> clazz, final EditConfigOnExitListener source, final String roomName,
			final Room roomConfiguration) throws ClassNotFoundException, java.lang.InstantiationException, IllegalAccessException {

		org.ambientlight.annotations.valueprovider.api.AlternativeValues alternatives = ValueBindingHelper
				.getValuesForClass(clazz.getAnnotation(AlternativeClassValues.class));

		editNewConfigBean(alternatives.classNames, ValueBindingHelper.toCharSequenceArray(alternatives.displayClassNames),
				source,
				roomName, roomConfiguration);
	}


	/**
	 * wrappermethod to create a bean if alternative values are already known. E.g. Values are given by a calling parent fragment.
	 * 
	 * @param altValues
	 * @param alternativeValuesForDisplay
	 * @param fragment
	 * @param server
	 * @param roomConfig
	 */
	protected static void editNewConfigBean(final List<String> altValues, final CharSequence[] alternativeValuesForDisplay,
			final EditConfigOnExitListener source, final String roomName, final Room roomConfig) {

		Activity activity = (Activity) (source instanceof Activity ? source : ((Fragment) source).getActivity());

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Bitte auswählen").setItems(alternativeValuesForDisplay, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				try {
					Serializable newBean = (Serializable) Class.forName(altValues.get(which)).newInstance();
					createNewChildFragent(source, newBean, roomName, roomConfig);
				} catch (Exception e) {
					Log.e(LOG, "could not create Object in createmode", e);
				}

			}
		});

		builder.create().show();
	}


	/**
	 * wrappermethod to edit a given bean.
	 * 
	 * @param fragment
	 *            to return to after editing is finished
	 * @param beanToEdit
	 * @param selectedServer
	 * @param roomConfig
	 */
	protected static void editConfigBean(EditConfigOnExitListener source, final Serializable beanToEdit,
			final String selectedRoom, final Room roomConfig) {

		Bundle args = new Bundle();
		args.putSerializable(EditConfigFragment.BUNDLE_OBJECT_VALUE, beanToEdit);
		createNewChildFragent(source, beanToEdit, selectedRoom, roomConfig);
	}


	/**
	 * starts a fragment transaction to create and configure an EditConfigFragment
	 * 
	 * @param source
	 *            calling fragment or activity where the edited bean will be given back
	 * @param selectedRoom
	 *            represents the room to that the bean is associated
	 * @param roomConfig
	 *            will be used to calculate alternative values for some of the input handlers
	 * @return
	 */
	private static void createNewChildFragent(EditConfigOnExitListener source, Serializable beanToEdit, String selectedRoom,
			Room roomConfig) {

		Bundle args = new Bundle();
		args.putString(ARG_SELECTED_ROOM, selectedRoom);
		args.putSerializable(ARG_ROOM_CONFIG, roomConfig);
		args.putSerializable(BUNDLE_OBJECT_VALUE, beanToEdit);

		EditConfigFragment configHandler = new EditConfigFragment();

		configHandler.setArguments(args);

		// caller may be another fragment or the beginning an activity
		FragmentTransaction ft;
		if (source instanceof EditConfigFragment) {
			configHandler.setTargetFragment((EditConfigFragment) source, REQ_RETURN_OBJECT);
			ft = ((EditConfigFragment) source).getFragmentManager().beginTransaction();
		} else {
			ft = ((FragmentActivity) source).getSupportFragmentManager().beginTransaction();
		}

		// fading in from right to center
		ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
		ft.replace(R.id.editConfigContainerLinearLayout, configHandler, FRAGMENT_TAG);
		// handle back action by the fragmentmanager
		ft.addToBackStack(null);
		ft.commit();
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_edit_configuration_menu, menu);
	}


	@Override
	public void onSaveInstanceState(Bundle bundle) {
		bundle.putSerializable(BUNDLE_OBJECT_VALUE, (Serializable) beanToEdit);
		bundle.putSerializable(BUNDLE_WHERE_TO_INTEGRATE, whereToMergeChildBean);
		super.onSaveInstanceState(bundle);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		selectedRoom = getArguments().getString(ARG_SELECTED_ROOM);
		this.roomConfig = (Room) getArguments().getSerializable(ARG_ROOM_CONFIG);

		if (savedInstanceState != null) {
			beanToEdit = savedInstanceState.getSerializable(BUNDLE_OBJECT_VALUE);
			whereToMergeChildBean = (WhereToMergeBean) savedInstanceState.getSerializable(BUNDLE_WHERE_TO_INTEGRATE);
		} else {
			beanToEdit = GuiUtils.deepCloneSerializeable(getArguments().getSerializable(BUNDLE_OBJECT_VALUE));
		}

		// integrate object into existing configuration after child fragment
		// closes and has data for us via onIntegrate - callback we do the
		// integration here after the arguments and bundles have been restored.
		// the other way it would be possible that this instance here would be
		// created by android and all values would be null.
		if (this.valueToIntegrate != null) {
			this.integrateConfiguration(this.valueToIntegrate);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// integrate object into existing configuration after child fragment has data left for us via onIntegrate. We do the
		// integration here after the arguments and bundles have been restored. We cannot merge the values in onCreate() because
		// it will only be called from android when the fragment instance has been removed before. e.g. screen rotation.
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
			ClassDescription description = beanToEdit.getClass().getAnnotation(ClassDescription.class);
			if (description != null) {
				for (Group currentGroup : description.groups()) {
					Map<Integer, Field> category = new TreeMap<Integer, Field>();
					sortedMap.put(currentGroup.position(), category);
				}
			} else {
				// create a default group
				sortedMap.put(0, new TreeMap<Integer, Field>());
			}

			// sort fields that are annotated in groups
			for (final Field field : beanToEdit.getClass().getFields()) {
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

				LinearLayout categoryView = (LinearLayout) inflater.inflate(R.layout.layout_editconfig_group_header, null);
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
					addFieldToView(inflater, beanToEdit, content, field);

					// we are shure that the default or a special category have
					// been used. if there are unsorted values put them into an
					// additional group later
					sortedValuesDrawn = true;
				}
			}

			// draw a header for unsorted fields if class description provided
			// categories for the other fields
			if (sortedValuesDrawn && unsortedList.isEmpty() == false) {
				LinearLayout categoryFurther = (LinearLayout) inflater.inflate(R.layout.layout_editconfig_group_header, null);
				TextView title = (TextView) categoryFurther.findViewById(R.id.textViewGroupHeader);
				title.setText("WEITERE");
				TextView descriptionTextView = (TextView) categoryFurther.findViewById(R.id.textViewGroupDescription);
				descriptionTextView.setVisibility(View.GONE);
				content.addView(categoryFurther);
			}

			// draw the handlers
			for (Field currentField : unsortedList) {
				addFieldToView(inflater, beanToEdit, content, currentField);
			}

			// default text if no fields are annotated
			if (sortedValuesDrawn == false && unsortedList.isEmpty()) {
				TextView tv = new TextView(content.getContext());
				tv.setText("Dieses Objekt wird nicht konfiguriert");
				content.addView(tv);
			}

			return result;

		} catch (Exception e) {
			Log.e(LOG, "could not create View for bean: " + beanToEdit.getClass().getName(), e);
			return null;
		}
	}


	private void addFieldToView(LayoutInflater inflater, final Object config, LinearLayout content, final Field field)
			throws IllegalAccessException, ClassNotFoundException, java.lang.InstantiationException {

		LinearLayout fieldView = (LinearLayout) inflater.inflate(R.layout.layout_editconfig_entry, null);
		content.addView(fieldView);

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

		// draw concrete view elements for each supported field type
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
			callParentOnIntegrate();
			return true;

		case android.R.id.home:
			callParentOnRevert();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}


	private void callParentOnRevert() {
		if (this.getTargetFragment() != null) {
			getFragmentManager().popBackStack();
			((EditConfigOnExitListener) this.getTargetFragment()).onRevertConfiguration(selectedRoom, beanToEdit);
		} else {
			((EditConfigOnExitListener) getActivity()).onRevertConfiguration(selectedRoom, beanToEdit);
		}
	}


	/*
	 * call parent(previous) view to integrate the edited configuration. It may be an EditFragment or at least the calling
	 * EditConfigActivity.
	 */
	private void callParentOnIntegrate() {
		if (this.getTargetFragment() != null) {
			getFragmentManager().popBackStack();
			((EditConfigOnExitListener) this.getTargetFragment()).onIntegrateConfiguration(selectedRoom, beanToEdit);
		} else {
			((EditConfigOnExitListener) getActivity()).onIntegrateConfiguration(selectedRoom, beanToEdit);
		}
	}


	/**
	 * We are listener to our own interface. So we can descent into sub objects recursively. We store the value in a global
	 * variable. The caller should terminate itself now and onCreateView() of ourself will be called. There we call
	 * integrateConfiguration() to persist the values in the bean that we are holding.
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


	/*
	 * will be called if there was an object posted by an child via onIntegrateConfiguration().
	 */
	private void integrateConfiguration(Object configuration) {
		try {
			Class<? extends Object> myConfigurationClass = beanToEdit.getClass();
			Field myField = myConfigurationClass.getField(whereToMergeChildBean.fieldName);

			if (whereToMergeChildBean.type.equals(WhereToPutType.FIELD)) {
				myField.set(beanToEdit, configuration);

			} else if (whereToMergeChildBean.type.equals(WhereToPutType.LIST)) {
				@SuppressWarnings("unchecked")
				// checked in line above
				List<Object> list = (List<Object>) myField.get(beanToEdit);
				if (whereToMergeChildBean.positionInList != null) {
					list.set(whereToMergeChildBean.positionInList, configuration);
				} else {
					list.add(configuration);
				}

			} else if (whereToMergeChildBean.type.equals(WhereToPutType.MAP)) {
				@SuppressWarnings("unchecked")
				// checked one line above
				Map<Object, Object> map = (Map<Object, Object>) myField.get(beanToEdit);
				map.put(whereToMergeChildBean.keyInMap, configuration);
			}

		} catch (Exception e) {
			Log.e(LOG, "error trying to integrate data from child into configuration", e);
		}

		// we do not want to be called again
		this.valueToIntegrate = null;
	}

}
