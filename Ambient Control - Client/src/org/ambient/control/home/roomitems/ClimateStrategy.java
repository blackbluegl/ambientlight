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
import org.ambientlight.room.entities.climate.util.MaxThermostateMode;
import org.ambientlight.room.entities.climate.util.MaxUtil;
import org.ambientlight.room.entities.features.Entity;
import org.ambientlight.room.entities.features.climate.Climate;
import org.ambientlight.room.entities.features.climate.TemperaturMode;
import org.ambientlight.ws.Room;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * @author Florian Bornkessel
 * 
 */
public class ClimateStrategy implements Strategy {

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

		updateIcon(itemContent, climate.getClimate());
		return itemContent;
	}


	private void updateIcon(RelativeLayout itemContent, TemperaturMode mode) {
		ImageView icon = (ImageView) itemContent.findViewById(R.id.imageViewHeatingIcon);

		boolean disabled = mode.temp <= MaxUtil.MIN_TEMPERATURE;

		if (mode.mode == MaxThermostateMode.AUTO || mode.mode == MaxThermostateMode.MANUAL
				|| mode.mode == MaxThermostateMode.TEMPORARY) {
			if (disabled) {
				icon.setImageResource(R.drawable.ic_heating_disabled);
			} else {
				icon.setImageResource(R.drawable.ic_heating_active);
			}
		} else if (mode.mode == MaxThermostateMode.BOOST) {
			icon.setImageResource(R.drawable.ic_heating_boost);
		}

		TextView modeText = (TextView) itemContent.findViewById(R.id.textViewTempMode);
		if (mode.mode == MaxThermostateMode.AUTO) {
			modeText.setText("A");
		}
		if (mode.mode == MaxThermostateMode.BOOST) {
			modeText.setText("B");
		}
		if (mode.mode == MaxThermostateMode.MANUAL) {
			modeText.setText("M");
		}
		if (mode.mode == MaxThermostateMode.TEMPORARY) {
			modeText.setText("T");
		}

		TextView tempText = (TextView) itemContent.findViewById(R.id.textViewTempDegree);
		tempText.setText(String.valueOf(mode.temp));
	}


	@Override
	public void onClick(View view, final Room room, final RoomFragment roomFragment, final Entity entity) {

		MaxThermostateMode maxThermostatemode = null;
		if (room.climateManager.mode == MaxThermostateMode.BOOST) {
			maxThermostatemode = room.climateManager.modeBeforeBoost;
		} else {
			maxThermostatemode = MaxThermostateMode.BOOST;
		}

		TemperaturMode mode = new TemperaturMode(room.climateManager.temperature, room.climateManager.temporaryUntilDate,
				maxThermostatemode);

		// update icon first and quick
		updateIcon((RelativeLayout) view, mode);
		try {
			RestClient.setTemperatureMode(roomFragment.roomName, mode);
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

	}

}
