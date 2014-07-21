package org.ambient.control.home.roomitems;

import org.ambient.control.R;


public class SimpleColorLightObjectStrategy extends LightObjectStrategy {

	@Override
	protected int getActiveIcon() {
		return R.drawable.ic_simple_color_active;
	}


	@Override
	protected int getDisabledIcon() {
		return R.drawable.ic_simple_color_disabled;
	}

}
