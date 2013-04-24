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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import de.devmil.common.ui.color.ColorSelectorView;


public class TronEditDialog extends DialogFragment {

	AlertDialog dialog;
	

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		boolean mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
		if(mIsLargeLayout){
			return null;	
		}
		else{
			return this.getView(inflater, container, savedInstanceState);
		}
	}

	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(this.getView(getActivity().getLayoutInflater(), null, savedInstanceState));
		builder.setPositiveButton(R.string.button_new, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
			}
		});
		
		builder.setNeutralButton(R.string.button_apply, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		
		builder.setNegativeButton(R.string.button_abort, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});

		this.dialog = builder.create();

        return this.dialog;
    }

    @Override
    public void onResume(){
    	super.onResume();
		Button theButton =  this.dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
		theButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
			//	RestClient.setProgramForLightObject(roomServer, scenery, lightObject, sc);
			}
			
		});
    }
    
    private View getView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
		LinearLayout contentView = (LinearLayout) inflater.inflate(R.layout.activity_program_tron, container, false);
  
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

			//sparkle length
			SeekBar seekBarSparkleLength = (SeekBar) contentView.findViewById(R.id.seekBarTronSparkleLength);
			seekBarSparkleLength.setProgress((int)config.getSparkleSize()*255);
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
					config.setSparkleSize((double)progress /255);
				}
			});
			
			//lightPoint length
			SeekBar seekBarLightPointLength = (SeekBar) contentView.findViewById(R.id.seekBarTronLightPointLength);
			seekBarLightPointLength.setProgress((int) (config.getTailLength()*255));
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
					config.setTailLength((double) progress/255 );
					
				}
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return contentView;
	}
}
