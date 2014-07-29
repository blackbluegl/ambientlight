package org.ambient.control.config;

import java.io.Serializable;

import org.ambient.control.R;
import org.ambientlight.ws.Room;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;


public class EditConfigActivity extends FragmentActivity implements EditConfigOnExitListener {

	protected static final String EXTRA_CREATE_CLASSNAME = "classNameToCreate";
	protected static final String EXTRA_ROOM_NAME = "bundleRoomName";
	protected static final String EXTRA_ROOM = "bundleRoom";
	protected static final String EXTRA_CREATE_MODE = "createMode";
	protected static final String EXTRA_EDIT_VALUE = "editValue";
	protected static final String EXTRA_RESULT_VALUE = "resultValue";

	public static final int REQUEST_EDIT_ROOM_ITEM = 0;


	public static void createInstanceForNewObject(Activity caller, String classNameToCreate, String roomName, Room room) {
		createInstance(EditConfigActivity.class, null, caller, true, classNameToCreate, null, roomName, room);
	}


	public static void createInstanceForEditObject(Class<? extends EditConfigActivity> className, Activity caller,
			Serializable valueToEditInRoom, String roomName, Room room) {
		createInstance(EditConfigActivity.class, null, caller, false, null, valueToEditInRoom, roomName, room);
	}


	protected static void createInstance(Class<? extends EditConfigActivity> className, Bundle bundle, Activity caller,
			boolean createMode, String classNameToCreate, Serializable valueToEditInRoom, String roomName, Room room) {

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

		// if (savedInstanceState == null) {

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
			Object valueToEdit = getIntent().getSerializableExtra(EXTRA_EDIT_VALUE);
			EditConfigFragment.editConfigBean(this, valueToEdit, roomName, room);
		}
		// }
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_config_activity, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
			return true;
		return super.onOptionsItemSelected(item);
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
		finishActivity(REQUEST_EDIT_ROOM_ITEM);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.config.EditConfigOnExitListener#onRevertConfiguration(java.lang.String, java.lang.Object)
	 */
	@Override
	public void onRevertConfiguration(String roomName, Object configuration) {
		setResult(Activity.RESULT_CANCELED);
		finishActivity(REQUEST_EDIT_ROOM_ITEM);
	}


	protected Serializable getValueToEdit() {

		EditConfigFragment fragment = ((EditConfigFragment) this.getSupportFragmentManager().findFragmentByTag(
				EditConfigFragment.FRAGMENT_TAG));
		return (Serializable) fragment.myConfigurationData;
	}
}
