package org.ambient.control.config;

import java.io.Serializable;

import org.ambient.control.R;
import org.ambientlight.ws.Room;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class EditConfigActivity extends FragmentActivity implements EditConfigOnExitListener {

	protected static final String EXTRA_CREATE_CLASSNAME = "classNameToCreate";
	protected static final String EXTRA_ROOM_NAME = "bundleRoomName";
	protected static final String EXTRA_CREATE_MODE = "createMode";
	protected static final String EXTRA_EDIT_VALUE = "editValue";

	public static final String EXTRA_ROOM = "bundleRoom";
	public static final String EXTRA_RESULT_VALUE = "resultValue";

	public static final int REQUEST_EDIT_ROOM_ITEM = 0;


	public static void createInstanceForNewObject(Activity caller, Class<?> classNameToCreate, String roomName, Room room) {
		createInstance(EditConfigActivity.class, null, caller, true, classNameToCreate, null, roomName, room);
	}


	public static void createInstanceForEditObject(Activity caller, Serializable valueToEditInRoom, String roomName, Room room) {
		createInstance(EditConfigActivity.class, null, caller, false, null, valueToEditInRoom, roomName, room);
	}


	protected static void createInstance(Class<? extends EditConfigActivity> className, Bundle bundle, Activity caller,
			boolean createMode, Class<?> classNameToCreate, Serializable valueToEditInRoom, String roomName, Room room) {

		// only childs may add their own bundles and use the stored values for themselve
		if (bundle == null) {
			bundle = new Bundle();
		}

		Intent request = new Intent(caller, className);
		request.putExtras(bundle);
		request.putExtra(EXTRA_CREATE_CLASSNAME, classNameToCreate);
		request.putExtra(EXTRA_ROOM_NAME, roomName);
		request.putExtra(EXTRA_ROOM, room);
		request.putExtra(EXTRA_CREATE_MODE, createMode);
		request.putExtra(EXTRA_EDIT_VALUE, valueToEditInRoom);

		caller.startActivityForResult(request, REQUEST_EDIT_ROOM_ITEM);

		caller.overridePendingTransition(0, 0);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(true);

		setContentView(R.layout.activity_edit_config_activity);

		if (savedInstanceState == null) {

			boolean createMode = getIntent().getBooleanExtra(EXTRA_CREATE_MODE, false);
			String roomName = getIntent().getStringExtra(EXTRA_ROOM_NAME);
			Room room = (Room) getIntent().getSerializableExtra(EXTRA_ROOM);

			if (createMode) {
				String className = getIntent().getStringExtra(EXTRA_CREATE_CLASSNAME);
				try {
					EditConfigFragment.editNewConfigBean(Class.forName(className), this, roomName, room);
				} catch (Exception e) {
					// should not happen here
					e.printStackTrace();
				}
			} else {
				Serializable valueToEdit = getIntent().getSerializableExtra(EXTRA_EDIT_VALUE);
				EditConfigFragment.editConfigBean(this, valueToEdit, roomName, room);
			}
		}
	}


	/*
	 * the fragments that call themselves recursively handle the back button via their fragment transactions. But at the end the
	 * we have to take care that we close the activity and return a result to the calling activity.
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
			onRevertConfiguration(getIntent().getStringExtra(EXTRA_ROOM_NAME), getIntent().getSerializableExtra(EXTRA_EDIT_VALUE));
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.config.EditConfigOnExitListener#onIntegrateConfiguration(java.lang.String, java.lang.Object)
	 */
	@Override
	public void onIntegrateConfiguration(String roomName, Object configuration) {
		Intent result = new Intent();
		result.putExtra(EXTRA_ROOM_NAME, roomName);
		result.putExtra(EXTRA_RESULT_VALUE, (Serializable) configuration);
		setResult(Activity.RESULT_OK, result);
		finish();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.config.EditConfigOnExitListener#onRevertConfiguration(java.lang.String, java.lang.Object)
	 */
	@Override
	public void onRevertConfiguration(String roomName, Object configuration) {
		Intent result = new Intent();
		result.putExtra(EXTRA_ROOM_NAME, roomName);
		result.putExtra(EXTRA_RESULT_VALUE, (Serializable) configuration);
		setResult(Activity.RESULT_CANCELED, result);
		finish();
	}


	protected Serializable getValueToEdit() {
		EditConfigFragment fragment = ((EditConfigFragment) this.getSupportFragmentManager().findFragmentByTag(
				EditConfigFragment.FRAGMENT_TAG));
		return (Serializable) fragment.beanToEdit;
	}
}
