package org.ambient.control.sceneries;

import org.ambient.control.MainActivity;
import org.ambient.control.R;
import org.ambient.control.rest.RestClient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NewSceneryDialogFragment extends DialogFragment {

	AlertDialog dialog;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		final LinearLayout dialogLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(
				R.layout.layout_sceneries_new_dialog, null);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setView(dialogLayout);
		builder.setTitle(R.string.title_new_scenery_name);
		
		builder.setPositiveButton(R.string.button_new, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				TextView textView = (TextView) dialogLayout.findViewById(R.id.editTextNewSceneryName);
				
				try {
					RestClient.createSceneryFromCurrent(((MainActivity) getActivity()).getSelectedRoomServer(), textView
							.getText().toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				String selectedServer = ((MainActivity) getActivity()).getSelectedRoomServer();
				((MainActivity) getActivity()).updateSceneriesForSelectedRoomServer(selectedServer);
			}
		});
		
		builder.setNegativeButton(R.string.button_abort, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
			}
		});
		
		dialog = builder.create();

		// disable the positive button on show
		dialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
			}
		});

		// disable/enable the positive button according the text amount
		EditText edit = (EditText) dialogLayout.findViewById(R.id.editTextNewSceneryName);
		edit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (count > 0) {
					((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				} else {
					((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		return dialog;
	}
}
