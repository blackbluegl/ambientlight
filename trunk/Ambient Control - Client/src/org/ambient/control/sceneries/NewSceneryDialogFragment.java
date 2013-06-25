package org.ambient.control.sceneries;

import org.ambient.control.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NewSceneryDialogFragment extends DialogFragment {

	public static String BUNDLE_SCENERY_NAME = "sceneryName";

	AlertDialog dialog;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {


		final LinearLayout dialogLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(
				R.layout.layout_sceneries_new_dialog, null);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setView(dialogLayout);
		builder.setTitle(R.string.title_new_scenery_name);

		String sceneryName = getArguments().getString(BUNDLE_SCENERY_NAME);
		TextView textView = (TextView) dialogLayout.findViewById(R.id.editTextNewSceneryName);
		textView.setText(sceneryName);

		builder.setPositiveButton(R.string.button_finish , new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				// TextView textView = (TextView)
				// dialogLayout.findViewById(R.id.editTextNewSceneryName);
				//
				// try {
				// RestClient.createOrUpdateSceneryFromCurrentScenery(((MainActivity)
				// getActivity()).getSelectedRoomServer(), textView
				// .getText().toString(),
				// getActivity().getApplicationContext());
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				//
				// String selectedServer = ((MainActivity)
				// getActivity()).getSelectedRoomServer();
				// ((MainActivity)
				// getActivity()).updateSceneriesForSelectedRoomServer(selectedServer);
			}
		});

		builder.setNegativeButton(R.string.button_abort, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
			}
		});

		dialog = builder.create();

		return dialog;
	}
}
