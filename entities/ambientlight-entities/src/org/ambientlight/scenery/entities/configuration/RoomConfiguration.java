package org.ambientlight.scenery.entities.configuration;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.drivers.configuration.DeviceConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("room")
public class RoomConfiguration {
	public String roomName;
	public int width;
	public int height;
	public List<LightObjectConfiguration> lightObjects = new ArrayList<LightObjectConfiguration>();
	public List<DeviceConfiguration> devices = new ArrayList<DeviceConfiguration>();
	
	public LightObjectConfiguration getLightObjectConfigurationByName(String name){
		for(LightObjectConfiguration current : lightObjects){
			if(current.lightObjectName.equals(name)){
				return current;
			}
		}
		return null;
	}
}
