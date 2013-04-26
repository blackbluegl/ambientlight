package org.ambient.control.sceneryconfiguration;

import org.ambient.control.R;
import org.ambientlight.scenery.SceneryConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.TronRenderingProgrammConfiguration;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import de.devmil.common.ui.color.ColorSelectorView;
import de.devmil.common.ui.color.ColorSelectorView.OnColorChangedListener;


public class TronEditDialog extends AbstractSceneryConfigEditDialogFragment {

	@Override
	protected SceneryConfiguration getCloneOfConfig(SceneryConfiguration config) {
		return ((TronRenderingProgrammConfiguration) config).clone();
	}


	@Override
	protected View getView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout to use as dialog or embedded fragment
		final LinearLayout contentView = (LinearLayout) inflater.inflate(R.layout.activity_program_tron, container, false);

		try {
			// background color
			final TronRenderingProgrammConfiguration config = (TronRenderingProgrammConfiguration) this.getConfig();
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

			// sparkle strength
			SeekBar seekBarSparcleStrength = (SeekBar) contentView.findViewById(R.id.seekBarTronSparkleStrength);
			seekBarSparcleStrength.setProgress((int) (config.getSparkleStrength() * 255));
			seekBarSparcleStrength.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

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
					config.setSparkleStrength((double) progress / 255);
				}
			});

			// sparkle length
			SeekBar seekBarSparkleLength = (SeekBar) contentView.findViewById(R.id.seekBarTronSparkleLength);
			seekBarSparkleLength.setProgress((int) config.getSparkleSize() * 255);
			seekBarSparkleLength.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

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
					config.setSparkleSize((double) progress / 255);
				}
			});

			// lightPoint length
			SeekBar seekBarLightPointLength = (SeekBar) contentView.findViewById(R.id.seekBarTronLightPointLength);
			seekBarLightPointLength.setProgress((int) (config.getTailLength() * 255));
			seekBarLightPointLength.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

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
					config.setTailLength((double) progress / 255);

				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

		return contentView;
	}


	@Override
	public int getTitle() {
		return R.string.title_edit_tron;
	}


	@Override
	protected SceneryConfiguration getNewSceneryConfig() {
		return new TronRenderingProgrammConfiguration();
	}
}
