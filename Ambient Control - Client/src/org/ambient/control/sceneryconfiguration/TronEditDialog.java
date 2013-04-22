package org.ambient.control.sceneryconfiguration;

import org.ambient.control.R;
import org.ambient.control.rest.RestClient;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.TronRenderingProgrammConfiguration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import de.devmil.common.ui.color.ColorSelectorView;


public class TronEditDialog extends DialogFragment {

	AlertDialog dialog;


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		final String scenery = getArguments().getString("scenery");
		final String lightObject = getArguments().getString("lightObject");
		final String roomServer = getArguments().getString("roomServer");

		try {
			// get config
			RoomConfiguration room = RestClient.getRoom(roomServer);
			RoomItemConfiguration current = room.getRoomItemConfigurationByName(lightObject);
			final TronRenderingProgrammConfiguration config = (TronRenderingProgrammConfiguration) current
					.getSceneryConfigurationBySceneryName(room.currentScenery);

			// preserve old values
			final TronRenderingProgrammConfiguration oldConfig = (TronRenderingProgrammConfiguration) config.clone();

			LinearLayout contentView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.activity_program_tron,
					null);

			builder.setView(contentView);
			builder.setTitle(R.string.title_activity_program_editor);

			// background color
			final ColorSelectorView colorPicker = (ColorSelectorView) contentView.findViewById(R.id.colorSelectorView);
			colorPicker.setColor(Color.rgb(config.getR(), config.getG(), config.getB()));

			// lightpoint amount
			SeekBar seekBarLightPointAmount = (SeekBar) contentView.findViewById(R.id.seekBarTronLightPointAmount);
			seekBarLightPointAmount.setProgress(config.getLightPointAmount() - 1);
			seekBarLightPointAmount.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {

				}


				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}


				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					config.setLightPointAmount(progress + 1);
				}
			});

			// speed
			SeekBar seekBarSpeed = (SeekBar) contentView.findViewById(R.id.seekBarTronSpeed);
			seekBarSpeed.setProgress((int) Math.sqrt(config.getSpeed() * 8));
			seekBarSpeed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub

				}


				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub

				}


				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					config.setSpeed((progress * progress) / (double) 8);
				}
			});

			// lightpoint impact
			SeekBar seekBarImpact = (SeekBar) contentView.findViewById(R.id.seekBarTronLightPointImpact);
			seekBarImpact.setProgress((int) (config.getLightImpact() * 255));
			seekBarImpact.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub

				}


				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub

				}


				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					config.setLightImpact((double) progress / 255);
				}
			});

			Button okButton = (Button) contentView.findViewById(R.id.buttonProgramEditorApply);
			okButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int color = colorPicker.getColor();
					config.setR(Color.red(color));
					config.setG(Color.green(color));
					config.setB(Color.blue(color));
					RestClient.setProgramForLightObject(roomServer, scenery, lightObject, config);
				}
			});

			builder.setPositiveButton(R.string.button_new, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {
				}
			});

			builder.setNegativeButton(R.string.button_abort, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {
					RestClient.setProgramForLightObject(roomServer, scenery, lightObject, oldConfig);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		dialog = builder.create();

		return dialog;
	}

}
