/*  Copyright 2013 Florian Bornkessel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.ambient.control.home.roomitems;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.ambient.control.R;
import org.ambient.rest.RestClient;
import org.ambient.views.TemperatureModeView;
import org.ambient.views.TemperatureModeView.ModeChangeListener;
import org.ambientlight.config.room.entities.climate.ClimateManagerConfiguration;
import org.ambientlight.config.room.entities.climate.TemperaturMode;
import org.ambientlight.room.entities.climate.util.MaxThermostateMode;
import org.ambientlight.room.entities.climate.util.MaxUtil;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;


/**
 * @author Florian Bornkessel
 * 
 */
public class TemperatureChooserDialogFragment extends DialogFragment implements ModeChangeListener {

	public static final String BUNDLE_CLIMATE_CONFIG = "bundleConfig";
	public static final String BUNDLE_ROOM_NAME = "bundleRoomName";

	ClimateManagerConfiguration config;
	String roomName;

	LinearLayout myContent;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		config = (ClimateManagerConfiguration) getArguments().getSerializable(BUNDLE_CLIMATE_CONFIG);
		roomName = getArguments().getString(BUNDLE_ROOM_NAME);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myContent = (LinearLayout) inflater.inflate(R.layout.fragment_home_room_climate, null);

		final TemperatureModeView tempChooser = (TemperatureModeView) myContent.findViewById(R.id.temperatureModeView);

		tempChooser.boostDurationInSeconds = config.boostDurationMins * 60;
		tempChooser.comfortTemp = config.comfortTemperatur;
		tempChooser.maxTemp = MaxUtil.MAX_TEMPERATURE;
		tempChooser.minTemp = MaxUtil.MIN_TEMPERATURE;
		tempChooser.setTemp(config.temperature);

		if (config.mode != MaxThermostateMode.BOOST) {
			tempChooser.setMode(config.mode);
		} else {
			long millisToGo = config.boostUntil.getTime() - System.currentTimeMillis();
			long secondsToGo = TimeUnit.MILLISECONDS.toSeconds(millisToGo);
			if (secondsToGo < 0) {
				secondsToGo = 0;
			}

			tempChooser.setBoostMode((int) secondsToGo);
		}

		Button okButton = (Button) myContent.findViewById(R.id.buttonTemperatureAccept);
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (tempChooser.getMode() == MaxThermostateMode.BOOST) {
					RestClient.setClimateBoostMode(roomName, true);

				} else if (tempChooser.getMode() == MaxThermostateMode.TEMPORARY) {
					CalendarView calendar = (CalendarView) myContent.findViewById(R.id.calendarView);
					Date until = new Date(calendar.getDate());
					TemperaturMode newMode = new TemperaturMode(tempChooser.getTemp(), until, tempChooser.getMode());
					RestClient.setTemperatureMode(roomName, newMode);

				} else {
					TemperaturMode newMode = new TemperaturMode(tempChooser.getTemp(), null, tempChooser.getMode());
					RestClient.setTemperatureMode(roomName, newMode);
				}
				dismiss();
			}
		});

		tempChooser.modeChangeListener = this;

		return myContent;
	}


	/*
	 * change visibility of date picker acording the choosen mode of the temperaturechooser
	 * 
	 * @see org.ambient.views.TemperatureModeView.ModeChangeListener#onModeChanged(java.lang.String)
	 */
	@Override
	public void onModeChanged(MaxThermostateMode mode) {
		CalendarView calendar = (CalendarView) myContent.findViewById(R.id.calendarView);
		if (mode == MaxThermostateMode.TEMPORARY) {
			calendar.setVisibility(View.VISIBLE);
		} else {
			calendar.setVisibility(View.GONE);
		}
	}
}
