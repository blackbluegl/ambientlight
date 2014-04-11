package org.ambient.control.home.mapper;

import org.ambient.control.R;
import org.ambientlight.room.entities.features.EntityId;

import android.view.View;

public class SunsetLightItemViewMapper extends AbstractRoomItemViewMapper{

	public SunsetLightItemViewMapper(View lightObject, EntityId id, int resourceId,
			boolean powerState) {
		super(lightObject, id, resourceId, powerState);
	}

	@Override
	protected int getActiveIcon() {
		return R.drawable.ic_simple_color_active;
	}

	@Override
	protected int getDisabledIcon() {
		return R.drawable.ic_simple_color_disabled;
	}

}
