package org.ambient.control.sceneryconfiguration;

import org.ambient.control.R;
import org.ambientlight.scenery.SceneryConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.SimpleColorRenderingProgramConfiguration;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import de.devmil.common.ui.color.ColorSelectorView;
import de.devmil.common.ui.color.ColorSelectorView.OnColorChangedListener;


public class SimpleColorEditDialog extends AbstractSceneryConfigEditDialogFragment {

	@Override
	protected SceneryConfiguration getCloneOfConfig(SceneryConfiguration config) {
		return ((SimpleColorRenderingProgramConfiguration) config).clone();
	}


	@Override
	protected View getView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final SimpleColorRenderingProgramConfiguration config = (SimpleColorRenderingProgramConfiguration) this.config;

		LinearLayout contentView = (LinearLayout) inflater.inflate(R.layout.activity_program_simplecolor, container, false);
		final ColorSelectorView colorPicker = (ColorSelectorView) contentView.findViewById(R.id.colorSelectorView);
		colorPicker.setColor(Color.rgb(config.getR(), config.getG(), config.getB()));
		colorPicker.setOnColorChangedListener(new OnColorChangedListener() {

			@Override
			public void colorChanged(int color) {
				config.setR(Color.red(color));
				config.setG(Color.green(color));
				config.setB(Color.blue(color));
			}
		});

		return contentView;
	}


	@Override
	public int getTitle() {
		return R.string.program_simple_color;
	}
}
