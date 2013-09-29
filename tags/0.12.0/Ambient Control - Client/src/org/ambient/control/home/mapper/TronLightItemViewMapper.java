package org.ambient.control.home.mapper;

import org.ambient.control.R;

import android.view.View;

public class TronLightItemViewMapper extends AbstractRoomItemViewMapper{

	public TronLightItemViewMapper(View lightObject, String label, int resourceId,
			boolean powerState) {
		super(lightObject, label, resourceId, powerState);
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
