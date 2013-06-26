package org.ambient.control.sceneryconfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import org.ambient.control.R;
import org.ambient.control.rest.RestClient;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.actors.ActorConfiguration;
import org.ambientlight.scenery.actor.ActorConductConfiguration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import de.devmil.common.ui.color.ColorSelectorView;
import de.devmil.common.ui.color.ColorSelectorView.OnColorChangedListener;


public class SceneryConfigEditDialogFragment extends DialogFragment {

	AlertDialog dialog;

	String scenery;
	String lightObject;
	String roomServer;
	String title;
	private ActorConductConfiguration config;
	private ActorConductConfiguration oldConfig;
	private boolean editAsNew = false;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (getResources().getBoolean(R.bool.large_layout))
			return null;
		else {
			try {
				this.extractFromBundle(savedInstanceState);
				return this.createViewByConfig(getConfig());
			} catch (Exception e) {
				return null;
			}
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
		builder.setTitle(this.title);
		try {
			builder.setView(this.createViewByConfig(config));
		} catch (Exception e) {
			e.printStackTrace();
		}

		builder.setPositiveButton(R.string.button_finish, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int id) {
				applyAction();
			}
		});

		builder.setNeutralButton(R.string.button_apply, new DialogInterface.OnClickListener() {

			// will be overwritten in onResume
			@Override
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
		// RestClient.setProgramForLightObject(roomServer, scenery, lightObject,
		// config);
	}


	public void cancelAction() {
		// RestClient.setProgramForLightObject(roomServer, scenery, lightObject,
		// oldConfig);
	}


	private void extractFromBundle(Bundle savedInstanceState) throws Exception {
		this.scenery = getArguments().getString("scenery");
		this.lightObject = getArguments().getString("lightObject");
		this.roomServer = getArguments().getString("roomServer");
		this.editAsNew = getArguments().getBoolean("editAsNew");

		this.title = getArguments().getString("title");

		RoomConfiguration room = RestClient.getRoom(roomServer);
		ActorConfiguration current = room.actorConfigurations.get(lightObject);

		if (editAsNew) {
			String configType = getArguments().getString("configType");
			this.config = (ActorConductConfiguration) Class.forName(configType).newInstance();
			this.oldConfig = current.actorConductConfiguration;
		} else {
			this.config = current.actorConductConfiguration;
			Method cloneMethod = config.getClass().getMethod("clone");
			this.oldConfig = (ActorConductConfiguration) cloneMethod.invoke(config);
		}
	}


	protected ActorConductConfiguration getConfig() {
		return this.config;
	}


	protected ActorConductConfiguration getOldConfig() {
		return this.oldConfig;
	}


	public View createViewByConfig(final ActorConductConfiguration config) throws IllegalArgumentException,
	IllegalAccessException {

		LinearLayout content = new LinearLayout(this.getActivity());
		content.setOrientation(LinearLayout.VERTICAL);

		Map<Integer, Field> sortedMap = new TreeMap<Integer, Field>();

		for (final Field field : config.getClass().getFields()) {
			if (field.isAnnotationPresent(TypeDef.class)) {
				if (field.isAnnotationPresent(Presentation.class)) {
					Presentation presentation = field.getAnnotation(Presentation.class);
					sortedMap.put(Integer.parseInt(presentation.position()), field);
				} else {
					addFieldToView(config, content, field, null);
				}
			}
		}

		if (sortedMap.size() > 0) {
			for (Field field : sortedMap.values()) {
				addFieldToView(config, content, field, field.getAnnotation(Presentation.class).name());
			}
		}
		return content;
	}


	private void addFieldToView(final ActorConductConfiguration config, LinearLayout content, final Field field, String name)
			throws IllegalAccessException {
		TypeDef description = field.getAnnotation(TypeDef.class);
		TextView label = new TextView(content.getContext());
		if (name != null) {
			label.setText(name);
		} else {
			label.setText(field.getName());
		}
		content.addView(label);

		if (description.fieldType().equals(FieldType.COLOR)) {
			ColorSelectorView colorView = new ColorSelectorView(content.getContext());
			colorView.setColor(field.getInt(config));
			content.addView(colorView);
			colorView.setOnColorChangedListener(new OnColorChangedListener() {

				@Override
				public void colorChanged(int color) {
					try {
						field.setInt(config, color);
					} catch (Exception e) {
						// this should not happen
					}
				}
			});
		}

		if (description.fieldType().equals(FieldType.NUMERIC)) {
			final double min = Double.parseDouble(description.min());
			final double difference = Double.parseDouble(description.max()) - min;

			SeekBar seekBar = new SeekBar(content.getContext());
			seekBar.setMax(256);
			double doubleValue = ((((Number) field.get(config)).doubleValue()) - min) / difference;
			seekBar.setProgress((int) (doubleValue * 256.0));
			content.addView(seekBar);
			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}


				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}


				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

					double result = (progress / 256.0) * difference + min;

					if (field.getType().equals(Double.TYPE)) {
						try {
							field.setDouble(config, result);
						} catch (Exception e) {
							// this should not happen
						}
					}

					if (field.getType().equals(Integer.TYPE)) {
						try {
							field.setInt(config, (int) result);
						} catch (Exception e) {
							// this should not happen
						}
					}
				}
			});
		}
	}


	public String getTitle() {
		return this.title;
	}
}
