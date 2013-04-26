package org.ambient.control.sceneryconfiguration;

import org.ambient.control.R;
import org.ambient.control.rest.RestClient;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;
import org.ambientlight.scenery.SceneryConfiguration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public abstract class AbstractSceneryConfigEditDialogFragment extends DialogFragment {

	AlertDialog dialog;

	String scenery;
	String lightObject;
	String roomServer;
	private SceneryConfiguration config;
	private SceneryConfiguration oldConfig;
	private boolean editAsNew = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (getResources().getBoolean(R.bool.large_layout)) {
			return null;
		} else {
			try {
				this.extractFromBundle(savedInstanceState);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return this.getView(inflater, container, savedInstanceState);
		}
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		try {
			this.extractFromBundle(savedInstanceState);
		} catch (Exception e) {
			e.printStackTrace();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(this.getTitle());
		builder.setView(this.getView(getActivity().getLayoutInflater(), null, savedInstanceState));

		builder.setPositiveButton(R.string.button_finish, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				applyAction();
			}
		});

		builder.setNeutralButton(R.string.button_apply, new DialogInterface.OnClickListener() {

			// will be overwritten in onResume
			public void onClick(DialogInterface dialog, int id) {
			}
		});

		builder.setNegativeButton(R.string.button_abort, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				cancelAction();
			}
		});

		this.dialog = builder.create();

		return this.dialog;
	}


	@Override
	public void onResume() {
		super.onResume();
		if (getResources().getBoolean(R.bool.large_layout)) {
			Button theButton = this.dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
			theButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					applyAction();
				}

			});
		}
	}


	public void applyAction() {
		RestClient.setProgramForLightObject(roomServer, scenery, lightObject, config);
	}


	public void cancelAction() {
		RestClient.setProgramForLightObject(roomServer, scenery, lightObject, oldConfig);
	}


	private void extractFromBundle(Bundle savedInstanceState) throws Exception {
		this.scenery = getArguments().getString("scenery");
		this.lightObject = getArguments().getString("lightObject");
		this.roomServer = getArguments().getString("roomServer");
		this.editAsNew = getArguments().getBoolean("editAsNew");

		RoomConfiguration room = RestClient.getRoom(roomServer, getActivity().getApplicationContext());
		RoomItemConfiguration current = room.getRoomItemConfigurationByName(lightObject);
		
		if(editAsNew){
			this.config = getNewSceneryConfig();
			this.oldConfig = current.getSceneryConfigurationBySceneryName(room.currentScenery);
		}
		else{
			this.config = current.getSceneryConfigurationBySceneryName(room.currentScenery);
			this.oldConfig = this.getCloneOfConfig(config);
		}
	}

	protected SceneryConfiguration getConfig(){
					return this.config;
	}

	protected SceneryConfiguration getOldConfig(){
		return this.oldConfig;
}

	
	protected abstract SceneryConfiguration getNewSceneryConfig();
	
	protected abstract SceneryConfiguration getCloneOfConfig(SceneryConfiguration config);


	protected abstract View getView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);


	public abstract int getTitle();
}
