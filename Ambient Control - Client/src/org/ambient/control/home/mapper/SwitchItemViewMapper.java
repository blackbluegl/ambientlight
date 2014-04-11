package org.ambient.control.home.mapper;

import org.ambient.control.R;
import org.ambientlight.room.entities.features.EntityId;

import android.view.View;

public class SwitchItemViewMapper extends AbstractRoomItemViewMapper {

	public SwitchItemViewMapper(View itemView, EntityId id, boolean powerState) {
		super(itemView, id, 0, powerState);
	}

	@Override
	protected int getActiveIcon() {
		return R.drawable.ic_switch_active;
	}

	@Override
	protected int getDisabledIcon() {
		return R.drawable.ic_switch_disabled;
	}

}
