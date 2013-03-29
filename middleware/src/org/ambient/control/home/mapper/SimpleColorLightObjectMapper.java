package org.ambient.control.home.mapper;

import org.ambient.control.R;

import android.view.View;

public class SimpleColorLightObjectMapper extends AbstractLightObjectIconMapper{

	public SimpleColorLightObjectMapper(View lightObject, String label, String serverName,
			boolean powerState) {
		super(lightObject, label, serverName, powerState);
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
