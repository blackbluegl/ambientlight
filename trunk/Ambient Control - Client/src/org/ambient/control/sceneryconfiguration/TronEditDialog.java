package org.ambient.control.sceneryconfiguration;

import java.util.concurrent.ExecutionException;

import org.ambient.control.R;
import org.ambient.control.rest.RestClient;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;
import org.ambientlight.scenery.SceneryConfiguration;
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
import de.devmil.common.ui.color.ColorSelectorView.OnColorChangedListener;


public class TronEditDialog extends DialogFragment {

	AlertDialog dialog;
	
	String scenery;
	String lightObject;
	String roomServer;
	SceneryConfiguration config;
	SceneryConfiguration oldConfig;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		boolean mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
		if(mIsLargeLayout){
			return null;	
		}
		else{
			this.extractFromBundle(savedInstanceState);
			return this.getView(inflater, container, savedInstanceState);
		}
	}

	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.extractFromBundle(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(this.getView(getActivity().getLayoutInflater(), null, savedInstanceState));
		builder.setPositiveButton(R.string.button_new, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int id) {
				RestClient.setProgramForLightObject(roomServer, scenery, lightObject, config);
			}
		});
		
		builder.setNeutralButton(R.string.button_apply, new DialogInterface.OnClickListener() {
			//will be overwritten in onResume
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		
		builder.setNegativeButton(R.string.button_abort, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				RestClient.setProgramForLightObject(roomServer, scenery, lightObject, oldConfig);
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
				RestClient.setProgramForLightObject(roomServer, scenery, lightObject, config);
			}
			
		});
    }
    
    private void extractFromBundle(Bundle savedInstanceState){
    	this.scenery = getArguments().getString("scenery");
		this.lightObject = getArguments().getString("lightObject");
		this.roomServer = getArguments().getString("roomServer");
		
		RoomConfiguration room;
		try {
			room = RestClient.getRoom(roomServer);
			RoomItemConfiguration current = room.getRoomItemConfigurationByName(lightObject);
			this.config = current.getSceneryConfigurationBySceneryName(room.currentScenery);
			// preserve old values
			this.oldConfig = (TronRenderingProgrammConfiguration) this.getCloneOfConfig(config);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private SceneryConfiguration getCloneOfConfig(SceneryConfiguration config){
		return ((TronRenderingProgrammConfiguration)config).clone();    	
    }
    
    private View getView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
		LinearLayout contentView = (LinearLayout) inflater.inflate(R.layout.activity_program_tron, container, false);
  
		

		try {
			// background color
			final TronRenderingProgrammConfiguration config = (TronRenderingProgrammConfiguration) this.config;
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
