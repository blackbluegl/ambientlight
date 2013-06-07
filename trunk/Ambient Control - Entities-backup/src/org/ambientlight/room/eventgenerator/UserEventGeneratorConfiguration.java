package org.ambientlight.room.eventgenerator;

import org.ambientlight.room.IUserRoomItem;


public class UserEventGeneratorConfiguration extends EventGeneratorConfiguration  implements IUserRoomItem {
	boolean powerState;
	@Override
	public boolean getPowerState() {
		return this.powerState;
	}

	@Override
	public void setPowerState(boolean powerState) {
		this.powerState= powerState;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name=name;
	}

}
