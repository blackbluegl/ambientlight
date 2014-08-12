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

import org.ambient.control.R;
import org.ambient.control.home.RoomFragment;
import org.ambient.rest.RestClient;
import org.ambient.util.GuiUtils;
import org.ambientlight.config.room.entities.climate.ClimateManagerConfiguration;
import org.ambientlight.config.room.entities.climate.TemperaturMode;
import org.ambientlight.room.entities.climate.util.MaxThermostateMode;
import org.ambientlight.room.entities.climate.util.MaxUtil;
import org.ambientlight.room.entities.features.Entity;
import org.ambientlight.room.entities.features.climate.Climate;
import org.ambientlight.ws.Room;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * displays climate icon. onClick will lead into a Boost. onLongClicking will show a dialog with an temperature widget.
 * 
 * @author Florian Bornkessel
 * 
 */
public class ClimateStrategy implements Strategy {

	ClimateManagerConfiguration climate;


	public ClimateStrategy(ClimateManagerConfiguration climate) {
		this.climate = climate;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.home.roomitems.Strategy#onCreateView(android.support.v4.app.Fragment,
	 * org.ambientlight.room.entities.features.Entity)
	 */
	@Override
	public View onCreateView(Fragment context, Entity entity) {

		Climate climate = (Climate) entity;

		LayoutInflater inflater = (LayoutInflater) context.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout itemContent = (RelativeLayout) inflater.inflate(R.layout.layout_room_item_heating_content, null);

		updateIcon(itemContent, climate.getTemperatureMode());
		return itemContent;
	}


	private void updateIcon(RelativeLayout itemContent, TemperaturMode mode) {
		ImageView icon = (ImageView) itemContent.findViewById(R.id.imageViewHeatingIcon);

		boolean disabled = mode.temp <= MaxUtil.MIN_TEMPERATURE + 0.1f;

		if (mode.thermostateMode == null) {
			icon.setImageResource(R.drawable.ic_heating_active);
		} else if (mode.thermostateMode == MaxThermostateMode.AUTO || mode.thermostateMode == MaxThermostateMode.MANUAL
				|| mode.thermostateMode == MaxThermostateMode.TEMPORARY) {
			if (disabled) {
				icon.setImageResource(R.drawable.ic_heating_disabled);
			} else {
				icon.setImageResource(R.drawable.ic_heating_active);
			}
		} else if (mode.thermostateMode == MaxThermostateMode.BOOST) {
			icon.setImageResource(R.drawable.ic_heating_boost);
		}

		TextView modeText = (TextView) itemContent.findViewById(R.id.textViewTempMode);
		if (mode.thermostateMode == null) {
			modeText.setText("");
		} else if (mode.thermostateMode == MaxThermostateMode.AUTO) {
			modeText.setText("A");
		} else if (mode.thermostateMode == MaxThermostateMode.BOOST) {
			modeText.setText("B");
		} else if (mode.thermostateMode == MaxThermostateMode.MANUAL) {
			modeText.setText("M");
		} else if (mode.thermostateMode == MaxThermostateMode.TEMPORARY) {
			modeText.setText("T");
		}

		TextView tempText = (TextView) itemContent.findViewById(R.id.textViewTempDegree);
		tempText.setText(String.valueOf(mode.temp));
		tempText.setTextColor(GuiUtils.getTemperatureTextColor(climate.temperature, climate.comfortTemperatur,
				MaxUtil.MAX_TEMPERATURE, MaxUtil.MIN_TEMPERATURE));
	}


	@Override
	public void onClick(View view, final Room room, final RoomFragment roomFragment, final Entity entity) {

		Climate climate = ((Climate) entity);

		TemperaturMode mode = new TemperaturMode(climate.getTemperatureMode().temp, climate.getTemperatureMode().until, null);

		try {
			if (climate.getTemperatureMode().thermostateMode == MaxThermostateMode.BOOST) {
				mode.thermostateMode = MaxThermostateMode.MANUAL;
				updateIcon((RelativeLayout) view, mode);
				RestClient.setClimateBoostMode(roomFragment.roomName, false);
			} else {
				mode.thermostateMode = MaxThermostateMode.BOOST;
				updateIcon((RelativeLayout) view, mode);
				RestClient.setClimateBoostMode(roomFragment.roomName, true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.home.roomitems.Strategy#onLongClick(android.view.View, org.ambientlight.ws.Room,
	 * org.ambient.control.home.RoomFragment, org.ambientlight.room.entities.features.Entity)
	 */
	@Override
	public void onLongClick(View view, Room room, RoomFragment roomFragment, Entity entity) {
		FragmentManager fm = roomFragment.getFragmentManager();

		Bundle args = new Bundle();
		args.putString(TemperatureChooserDialogFragment.BUNDLE_ROOM_NAME, room.roomName);
		args.putSerializable(TemperatureChooserDialogFragment.BUNDLE_CLIMATE_CONFIG, room.climateManager);

		TemperatureChooserDialogFragment tempFragment = new TemperatureChooserDialogFragment();
		tempFragment.setArguments(args);

		tempFragment.show(fm, "tempChooserFragment");
	}

}
