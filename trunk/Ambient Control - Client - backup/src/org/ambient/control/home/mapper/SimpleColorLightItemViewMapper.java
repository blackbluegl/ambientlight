package org.ambient.control.home.mapper;

import org.ambient.control.R;

import android.view.View;

public class SimpleColorLightItemViewMapper extends AbstractRoomItemViewMapper{

	public SimpleColorLightItemViewMapper(View lightObject, String label, int resourceId, String serverName,
			boolean powerState, boolean bypassSceneryChange) {
		super(lightObject, label, resourceId, serverName, powerState, bypassSceneryChange);
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
