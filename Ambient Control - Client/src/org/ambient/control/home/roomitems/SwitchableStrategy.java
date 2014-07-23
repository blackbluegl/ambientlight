package org.ambient.control.home.roomitems;

import org.ambient.control.R;
import org.ambient.control.home.RoomFragment;
import org.ambient.rest.RestClient;
import org.ambientlight.room.entities.features.Entity;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.ws.Room;

import android.view.View;
import android.widget.ImageView;


/**
 * 
 * for all switches. turn switches off and on by clicking. LongClick is not used.
 * 
 * @author Florian Bornkessel
 * 
 */
public class SwitchableStrategy extends LightObjectStrategy {

	@Override
	public void onClick(View view, final Room room, final RoomFragment roomFragment, final Entity entity) {

		Switchable entityAsSwitch = (Switchable) entity;

		// update the icon - we prevent that user experiences a
		// gap (request to server, response to callback)
		updateIcon(!entityAsSwitch.getPowerState(), (ImageView) view);

		try {
			RestClient.setSwitchablePowerState(roomFragment.roomName, entityAsSwitch.getId(), !entityAsSwitch.getPowerState());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/*
	 * no long click available for simple switches.
	 */
	@Override
	public void onLongClick(View view, final Room room, final RoomFragment roomFragment, final Entity entity) {
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.home.mapper.LightObjectStrategy#getActiveIcon()
	 */
	@Override
	protected int getActiveIcon() {
		return R.drawable.ic_switch_active;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambient.control.home.mapper.LightObjectStrategy#getDisabledIcon()
	 */
	@Override
	protected int getDisabledIcon() {
		return R.drawable.ic_switch_disabled;
	}
}
