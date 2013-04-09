package org.ambientlight.room;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.drivers.DeviceConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("room")
public class RoomConfiguration {
	public String roomName;
	public int width;
	public int height;
	public List<RoomItemConfiguration> roomItemConfigurations = new ArrayList<RoomItemConfiguration>();
	public List<DeviceConfiguration> deviceConfigurations = new ArrayList<DeviceConfiguration>();

	
	public RoomItemConfiguration getRoomItemConfigurationByName(String name){
		for(RoomItemConfiguration current : roomItemConfigurations){
			if(current.name.equals(name)){
				return current;
			}
		}
		return null;
	}
}
