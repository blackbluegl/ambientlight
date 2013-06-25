package org.ambient.control.home.mapper;

import org.ambient.control.R;

import android.view.View;

public class SwitchItemViewMapper extends AbstractRoomItemViewMapper {

	public SwitchItemViewMapper(View itemView, String itemName, String serverName, boolean powerState, boolean bypassSceneryChange) {
		super(itemView, itemName,0, serverName, powerState, bypassSceneryChange);
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
