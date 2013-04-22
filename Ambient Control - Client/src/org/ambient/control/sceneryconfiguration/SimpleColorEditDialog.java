package org.ambient.control.sceneryconfiguration;

import org.ambient.control.R;
import org.ambient.control.rest.RestClient;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.SimpleColorRenderingProgramConfiguration;

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
import de.devmil.common.ui.color.ColorSelectorView;

public class SimpleColorEditDialog extends DialogFragment {

	AlertDialog dialog;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		final String scenery = getArguments().getString("scenery");
		final String lightObject = getArguments().getString("lightObject");
		final String roomServer = getArguments().getString("roomServer");

		try {
			int color = 0;
			final int oldColor;

			RoomConfiguration room = RestClient.getRoom(roomServer);
			RoomItemConfiguration current = room.getRoomItemConfigurationByName(lightObject);
			SimpleColorRenderingProgramConfiguration config = (SimpleColorRenderingProgramConfiguration) current
					.getSceneryConfigurationBySceneryName(room.currentScenery);
			color = Color.rgb(config.getR(), config.getG(), config.getB());
			oldColor = Color.rgb(config.getR(), config.getG(), config.getB());

			LinearLayout contentView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.activity_program_simplecolor,
					null);

			builder.setView(contentView);
			builder.setTitle(R.string.title_activity_program_editor);

			final ColorSelectorView colorPicker = (ColorSelectorView) contentView.findViewById(R.id.colorSelectorView);
			colorPicker.setColor(color);

			Button okButton = (Button) contentView.findViewById(R.id.buttonProgramEditorApply);
			okButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int color = colorPicker.getColor();
					SimpleColorRenderingProgramConfiguration sc = new SimpleColorRenderingProgramConfiguration();
					sc.setR(Color.red(color));
					sc.setG(Color.green(color));
					sc.setB(Color.blue(color));
					RestClient.setProgramForLightObject(roomServer, scenery, lightObject, sc);
				}
			});

			builder.setPositiveButton(R.string.button_new, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});

			builder.setNegativeButton(R.string.button_abort, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					SimpleColorRenderingProgramConfiguration sc = new SimpleColorRenderingProgramConfiguration();
					sc.setR(Color.red(oldColor));
					sc.setG(Color.green(oldColor));
					sc.setB(Color.blue(oldColor));
					RestClient.setProgramForLightObject(roomServer, scenery, lightObject, sc);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		dialog = builder.create();

		return dialog;
	}

}
